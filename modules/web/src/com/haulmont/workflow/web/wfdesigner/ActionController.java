/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.wfdesigner;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.workflow.core.app.CardPropertyHandlerLoaderService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.enums.OperationsType;
import com.haulmont.workflow.core.global.CardPropertyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author gorodnov
 * @version $Id$
 */
@Controller
@RequestMapping("/wfdesigner/*/action/*.json")
public class ActionController {

    protected Log log = LogFactory.getLog(getClass());

    @Inject
    protected DataService dataService;

    @Inject
    protected Messages messages;

    @RequestMapping(method = RequestMethod.POST)
    public String handlePostRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "id", required = false) String id) {
        try {
            Design design;
            if (auth(request, response) && (design = findDesign(request, response, id)) != null) {
                String src = IOUtils.toString(request.getInputStream(), StandardCharsets.UTF_8);
                JSONObject jsonObject = new JSONObject(src);
                String name = jsonObject.getString("name");

                try {
                    design.setName(name);
                    design.setSrc(src);
                    design.setCompileTs(null);

                    CommitContext commitContext = new CommitContext(Collections.singleton(design));
                    dataService.commit(commitContext);

                    setHeaders(response);
                    PrintWriter out = response.getWriter();
                    out.println("{\"error\": null}");
                    out.close();
                } finally {
                    AppContext.setSecurityContext(null);
                }
            }
            return null;
        } catch (Throwable t) {
            log.error("Error processing POST", t);
            throw new RuntimeException(t);
        }
    }

    @RequestMapping(value = "/wfdesigner/*/action/checkExpression.json", method = RequestMethod.GET)
    public String checkExpression(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "value") String value,
            @RequestParam(value = "type") String type
    ) {
        try {
            if (auth(request, response)) {
                try {
                    CardPropertyHandlerLoaderService workflowSettingsService = AppBeans.get(CardPropertyHandlerLoaderService.NAME);

                    AttributeType attributeType = AttributeType.fromId(type);
                    String data = "";
                    if (attributeType != null) {
                        Class clazz = CardPropertyUtils.getSimpleClassFromAttributeType(attributeType);
                        if (clazz != null) {
                            String result = workflowSettingsService.getLocalizedValue(clazz, true, value);
                            if (result != null) {
                                data = result;
                            }
                        }
                    }
                    setHeaders(response);
                    PrintWriter out = response.getWriter();
                    out.println(data);
                    out.close();
                    return null;
                } finally {
                    AppContext.setSecurityContext(null);
                }
            }
            return null;
        } catch (Throwable t) {
            log.error("Error processing GET", t);
            throw new RuntimeException("Error processing GET", t);
        }
    }

    @RequestMapping(value = "/wfdesigner/*/action/propertyPath.json", method = RequestMethod.GET)
    public String handleLoadPropertyPathsGetRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "query") String query,
            @RequestParam(value = "class") String className

    ) {
        try {
            if (auth(request, response)) {
                try {
                    String start = StringUtils.substringAfterLast(query, ".");
                    String propertyPath = StringUtils.substringBeforeLast(query, ".");
                    if (!query.contains(".")) {
                        start = propertyPath;
                        propertyPath = "";
                    }
                    Metadata metadata = AppBeans.get(Metadata.NAME);
                    MetaClass metaClass = metadata.getSession().getClass(Class.forName(className));
                    List<PropertyPath> propertyPaths = getPropertyPaths(metaClass, propertyPath, start);
                    JSONWriter json = new JSONStringer().array();
                    for (PropertyPath path : propertyPaths) {
                        path.toJsonWriter(json);
                    }
                    json.endArray();
                    printJson(response, json.toString());

                } finally {
                    AppContext.setSecurityContext(null);
                }
            }
            return null;
        } catch (Throwable t) {
            log.error("Error on load property paths GET", t);
            throw new RuntimeException("Error on load property paths GET", t);
        }
    }

    protected List<PropertyPath> getPropertyPaths(MetaClass metaClass, String propertyPath, String start) {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        CardPropertyHandlerLoaderService workflowSettingsService = AppBeans.get(CardPropertyHandlerLoaderService.NAME);
        Class clazz = StringUtils.isBlank(propertyPath) ? metaClass.getJavaClass() : CardPropertyUtils.getClassByMetaProperty(metaClass, propertyPath);
        List<PropertyPath> paths = new ArrayList<>();
        if (clazz == null) {
            String systemPropertyPath = CardPropertyUtils.getSystemPathByMetaProperty(metaClass, propertyPath);
            clazz = CardPropertyUtils.getClassByMetaProperty(metaClass, systemPropertyPath);
            if (clazz == null) {
                return paths;
            }
        }
        metaClass = metadata.getSession().getClass(clazz);
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            Class declaringClazz = property.getDeclaringClass();
            String localizePropertyName = propertyName;
            boolean isEntity = AttributeType.ENTITY.equals(workflowSettingsService.getAttributeType(property.getJavaType(), false));
            if (declaringClazz != null) {
                localizePropertyName = messages.getMessage(declaringClazz.getPackage().getName(),
                        declaringClazz.getSimpleName() + "." + propertyName)
                        .replace(".", "");
            }
            if (!Arrays.asList(Range.Cardinality.MANY_TO_MANY, Range.Cardinality.ONE_TO_MANY).contains(property.getRange().getCardinality())) {
                if (StringUtils.isBlank(start) || StringUtils.containsIgnoreCase(localizePropertyName, start)) {
                    paths.add(new PropertyPath(propertyPath, propertyName, localizePropertyName, isEntity));
                } else if (StringUtils.containsIgnoreCase(propertyName, start)) {
                    paths.add(new PropertyPath(propertyPath, propertyName, propertyName, isEntity));
                }
            }
        }
        Collections.sort(paths);
        return paths;
    }

    @RequestMapping(value = "/wfdesigner/*/action/loadAttributeType.json", method = RequestMethod.GET)
    public String handleLoadAttributeGetRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "path") String propertyPath,
            @RequestParam(value = "class") String className
    ) {
        try {
            if (auth(request, response)) {
                try {
                    if (StringUtils.isNotBlank(propertyPath) && StringUtils.isNotBlank(className)) {
                        Metadata metadata = AppBeans.get(Metadata.NAME);
                        MetaClass metaClass = metadata.getSession().getClass(Class.forName(className));
                        String systemPropertyPath = CardPropertyUtils.getSystemPathByMetaProperty(metaClass, propertyPath);
                        Class clazz = CardPropertyUtils.getClassByMetaProperty(metaClass, systemPropertyPath);
                        if (clazz == null) {
                            response.sendError(HttpServletResponse.SC_NO_CONTENT, "Unreachable property path");
                            return null;
                        }
                        String data = loadAttributeType(clazz, systemPropertyPath);
                        printJson(response, data);
                    }
                } finally {
                    AppContext.setSecurityContext(null);
                }
            }
            return null;
        } catch (Throwable t) {
            log.error("Error on load entity attributes GET", t);
            throw new RuntimeException("Error on load entity attributes GET", t);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public String handleGetRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "id", required = false) String id) {
        try {
            if (auth(request, response)) {
                try {
                    Design design;
                    if (request.getPathInfo().endsWith("/load.json") && (design = findDesign(request, response, id)) != null) {
                        String src = design.getSrc() == null ? "" : design.getSrc();
                        printJson(response, "[" + src + "]");
                    } else if (request.getPathInfo().endsWith("/loadScripts.json")) {
                        JSONWriter json = new JSONStringer().array();
                        for (DesignScript designScript : findDesignScripts(request, response, id))
                            json.value(designScript.getName());
                        json.endArray();
                        printJson(response, json.toString());
                    } else if (request.getPathInfo().endsWith("/loadJbpmProcs.json")) {
                        JSONWriter json = new JSONStringer().array();
                        for (Proc proc : findProcs())
                            json.object()
                                    .key("procCode").value(proc.getCode())
                                    .key("name").value(proc.getName())
                                    .endObject();
                        json.endArray();
                        printJson(response, json.toString());
                    } else if (request.getPathInfo().endsWith("/loadSubDesigns.json")) {
                        JSONWriter json = new JSONStringer().array();
                        for (Design subDesign : loadSubDesigns()) {
                            JSONObject subDesignJson = new JSONObject(subDesign.getSrc());

                            JSONObject jsWorking = subDesignJson.getJSONObject("working");
                            JSONArray jsModules = jsWorking.getJSONArray("modules");

                            json.object()
                                    .key("value").value(subDesign.getId().toString())
                                    .key("label").value(subDesign.getName())
                                    .key("outs").array();

                            for (int i = 0; i < jsModules.length(); i++) {
                                JSONObject jsModule = jsModules.getJSONObject(i);
                                String name = jsModule.getString("name");
                                if (name.equals("End")) {
                                    String outName = jsModule.getJSONObject("value").getJSONObject("options").getString("name");
                                    json.object()
                                            .key("name").value(outName).endObject();
                                }
                            }

                            json.endArray().endObject();
                        }
                        json.endArray();
                        printJson(response, json.toString());
                    } else if (request.getPathInfo().endsWith("/loadOperationTypes.json")) {
                        Messages messages = AppBeans.get(Messages.NAME);
                        JSONWriter json = new JSONStringer().array();
                        for (OperationsType operationsType : Arrays.asList(OperationsType.values())) {
                            json.object()
                                    .key("value").value(operationsType.getId())
                                    .key("label").value(messages.getMessage(operationsType))
                                    .endObject();
                        }
                        json.endArray();
                        printJson(response, json.toString());
                    } else if (request.getPathInfo().endsWith("/loadCardInheritors.json")) {
                        JSONWriter json = new JSONStringer().array();
                        for (MetaClass cl : findClasses()) {
                            String name = messages.getMessage(cl.getJavaClass().getPackage().getName(), cl.getJavaClass().getSimpleName());
                            json.object()
                                    .key("key").value(cl.getJavaClass().getName())
                                    .key("name").value(name)
                                    .endObject();
                        }
                        json.endArray();
                        printJson(response, json.toString());
                    } else {
                        log.warn("Illegal request path info: " + request.getPathInfo());
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    }
                } finally {
                    AppContext.setSecurityContext(null);
                }
            }
            return null;
        } catch (Throwable t) {
            log.error("Error processing GET", t);
            throw new RuntimeException(t);
        }
    }

    protected Collection<MetaClass> findClasses() {
        MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);
        Metadata metadata = AppBeans.get(Metadata.NAME);
        List<MetaClass> cardInheritors = new ArrayList<>();
        final MetaClass cardMetaClass = metadata.getClass(Card.class);
        for (MetaClass metaClass : metadataTools.getAllPersistentMetaClasses()) {
            if (metaClass.getAncestors().contains(cardMetaClass) || metaClass.equals(cardMetaClass)) {
                cardInheritors.add(metaClass);
            }
        }
        return cardInheritors;
    }

    protected void setHeaders(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
    }

    protected void printJson(HttpServletResponse response, String json) throws IOException {
        setHeaders(response);
        PrintWriter out = response.getWriter();
        out.println(json);
        out.close();
    }


    protected List<Design> loadSubDesigns() {
        LoadContext ctx = new LoadContext(Design.class).setView("load-subdesign");
        ctx.setQueryString("select d from wf$Design d where d.type=:type and d.compileTs is not null order by d.name").setParameter("type", DesignType.SUBDESIGN.getId());
        return dataService.loadList(ctx);

    }

    protected Design findDesign(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No design ID provided");
            return null;
        }
        UUID designId = UUID.fromString(id);
        Design design = loadDesign(designId);
        if (design == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Design not found");
            return null;
        }
        return design;
    }

    protected List<DesignScript> findDesignScripts(HttpServletRequest request, HttpServletResponse response, String id) throws IOException {
        if (id == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No design ID provided");
            return null;
        }
        UUID designId = UUID.fromString(id);
        return loadDesignScripts(designId);
    }

    protected boolean auth(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserSession userSession = ControllerUtils.getUserSession(request);
        if (userSession == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
        AppContext.setSecurityContext(new SecurityContext(userSession));
        return true;
    }

    protected List<Proc> findProcs() {
        View view = new View(Proc.class).addProperty("code").addProperty("name");
        LoadContext ctx = new LoadContext(Proc.class).setView(view);
        ctx.setQueryString("select p from wf$Proc p order by p.name");
        return dataService.loadList(ctx);
    }

    protected Design loadDesign(UUID designId) {
        return dataService.load(new LoadContext<>(Design.class).setId(designId).setView("_local"));
    }

    protected List<DesignScript> loadDesignScripts(UUID designId) {
        LoadContext ctx = new LoadContext(DesignScript.class).setView("_minimal");
        ctx.setQueryString("select s from wf$DesignScript s where s.design.id = :designId").setParameter("designId", designId);
        return dataService.loadList(ctx);
    }

    protected String loadAttributeType(Class clazz, String systemPropertyPath) throws JSONException {
        Messages messages = AppBeans.get(Messages.NAME);
        CardPropertyHandlerLoaderService workflowSettingsService = AppBeans.get(CardPropertyHandlerLoaderService.NAME);
        AttributeType attributeType = workflowSettingsService.getAttributeType(clazz, false);

        JSONWriter json = new JSONStringer().array();
        json.object()
                .key("attributeType").value(attributeType.getId())
                .key("ops").array();
        for (OperationsType operationsType : OperationsType.availableOps(attributeType)) {
            json.object()
                    .key("value").value(operationsType.getId())
                    .key("label").value(messages.getMessage(operationsType))
                    .endObject();
        }
        json.endArray();
        json.key("values").array();

        Map<String, Object> map = workflowSettingsService.loadObjects(clazz, false);
        for (String key : map.keySet()) {
            json.object()
                    .key("value").value(key)
                    .key("label").value(StringEscapeUtils.escapeHtml(map.get(key).toString()))
                    .endObject();
        }
        json.endArray();
        if (systemPropertyPath != null) {
            json.key("systemPropertyPath").value(systemPropertyPath);
        }
        json.endObject();
        json.endArray();
        return json.toString();
    }

    protected static class PropertyPath implements Comparable<PropertyPath> {

        protected String path;
        protected String property;
        protected String locProperty;
        protected Boolean entity;

        public PropertyPath(String path, String property, String locProperty, Boolean isEntity) {
            this.path = path;
            this.property = property;
            this.locProperty = locProperty;
            this.entity = isEntity;
        }

        @Override
        public int compareTo(PropertyPath o) {
            if (o == null) return 1;
            return ObjectUtils.compare(this.property, o.property);
        }

        public JSONWriter toJsonWriter(JSONWriter json) throws JSONException {
            return json.object()
                    .key("path").value(this.path)
                    .key("property").value(this.property)
                    .key("locProperty").value(this.locProperty)
                    .key("isEntity").value(this.entity)
                    .endObject();
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getProperty() {
            return property;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public String getLocProperty() {
            return locProperty;
        }

        public void setLocProperty(String locProperty) {
            this.locProperty = locProperty;
        }

        public Boolean isEntity() {
            return entity;
        }

        public void setEntity(Boolean isEntity) {
            this.entity = isEntity;
        }
    }
}
