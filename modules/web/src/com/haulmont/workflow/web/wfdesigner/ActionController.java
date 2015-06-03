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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONWriter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
                String src = IOUtils.toString(request.getInputStream(), "UTF-8");
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
                        List<String> propertyPaths = getPropertyPaths(metaClass,propertyPath, start);
                        StringBuilder sb = new StringBuilder("[");
                        int i = 0;
                        for (String path : propertyPaths) {
                            if (i > 0) {
                                sb.append(", ");
                            }
                            sb.append("\"");
                            sb.append(propertyPath);
                            if (StringUtils.isNotBlank(propertyPath)) {
                                sb.append(".");
                            }
                            sb.append(path);
                            sb.append("\"");
                            i++;
                        }
                        sb.append("]");
                        setHeaders(response);
                        PrintWriter out = response.getWriter();
                        out.println(sb.toString());
                        out.close();
                        return null;

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

    private List<String> getPropertyPaths(MetaClass metaClass, String propertyPath, String start) {
        Metadata metadata = AppBeans.get(Metadata.NAME);
        Class clazz = StringUtils.isBlank(propertyPath) ? Card.class : CardPropertyUtils.getClassByMetaProperty(metaClass, propertyPath);
        List<String> paths = new ArrayList<>();
        if (clazz == null) {
            return paths;
        }
        metaClass = metadata.getSession().getClass(clazz);
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            if (!Arrays.asList(Range.Cardinality.MANY_TO_MANY, Range.Cardinality.ONE_TO_MANY).contains(property.getRange().getCardinality())) {
                if (StringUtils.isBlank(start) || StringUtils.containsIgnoreCase(propertyName, start)) {
                    paths.add(propertyName);
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
                        Class clazz = CardPropertyUtils.getClassByMetaProperty(metaClass, propertyPath);
                        if (clazz == null) {
                            response.sendError(HttpServletResponse.SC_NO_CONTENT, "Unreachable property path");
                            return null;
                        }
                        String data = loadAttributeType(clazz);
                        setHeaders(response);
                        PrintWriter out = response.getWriter();
                        out.println(data);
                        out.close();
                        return null;
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
                        String data = loadOperationTypes();
                        setHeaders(response);
                        PrintWriter out = response.getWriter();
                        out.println(data);
                        out.close();
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

    private Collection<MetaClass> findClasses() {
        MetadataTools metadataTools = AppBeans.get(MetadataTools.NAME);
        Metadata metadata = AppBeans.get(Metadata.NAME);
        Collection<MetaClass> allPersistentMetaClasses = metadataTools.getAllPersistentMetaClasses();
        List<MetaClass> cardInheritors = new ArrayList<>();
        final MetaClass cardMetaClass = metadata.getClass(Card.class);
        for (MetaClass metaClass : allPersistentMetaClasses) {
            if (metaClass.getAncestors().contains(cardMetaClass) || metaClass.equals(cardMetaClass)) {
                cardInheritors.add(metaClass);
            }
        }
        return cardInheritors;
    }

    protected void setHeaders(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
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

    private String loadOperationTypes() {
        StringBuilder sb = new StringBuilder();
        Messages messages = AppBeans.get(Messages.NAME);
        sb.append("[");
        List<OperationsType> operationsTypes = Arrays.asList(OperationsType.values());
        int size = operationsTypes.size();
        for (OperationsType operationsType : operationsTypes) {
            sb.append("{");
            sb.append("\"value\" : ");
            sb.append("\"").append(operationsType.getId()).append("\",");
            sb.append("\"label\" : ");
            sb.append("\"").append(messages.getMessage(operationsType)).append("\"");
            sb.append("}");
            size--;
            if (size > 0)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
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
        LoadContext ctx = new LoadContext(Design.class).setId(designId).setView("_local");
        return dataService.load(ctx);
    }

    protected List<DesignScript> loadDesignScripts(UUID designId) {
        LoadContext ctx = new LoadContext(DesignScript.class).setView("_minimal");
        ctx.setQueryString("select s from wf$DesignScript s where s.design.id = :designId").setParameter("designId", designId);
        return dataService.loadList(ctx);
    }

    private String loadAttributeType(Class clazz) {
        Messages messages = AppBeans.get(Messages.NAME);
        CardPropertyHandlerLoaderService workflowSettingsService = AppBeans.get(CardPropertyHandlerLoaderService.NAME);
        AttributeType attributeType = workflowSettingsService.getAttributeType(clazz, false);
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append("{\"attributeType\":\"").append(attributeType.getId()).append("\", ");
        EnumSet<OperationsType> operationsTypes = OperationsType.availableOps(attributeType);
        sb.append("\"ops\":");
        sb.append("[");
        int size = operationsTypes.size();
        for (OperationsType operationsType : operationsTypes) {
            sb.append("{");
            sb.append("\"value\" : ");
            sb.append("\"").append(operationsType.getId()).append("\",");
            sb.append("\"label\" : ");
            sb.append("\"").append(messages.getMessage(operationsType)).append("\"");
            sb.append("}");
            size--;
            if (size > 0)
                sb.append(",");
        }
        sb.append("],");
        sb.append("\"values\":");
        sb.append("[");
        Map<String, Object> map = workflowSettingsService.loadObjects(clazz, false);
        size = map.size();
        for (String key : map.keySet()) {
            sb.append("{");
            sb.append("\"value\" : ");
            sb.append("\"").append(key).append("\",");
            sb.append("\"label\" : ");
            sb.append("\"").append(StringEscapeUtils.escapeJavaScript(map.get(key).toString())).append("\"");
            sb.append("}");
            size--;
            if (size > 0)
                sb.append(",");
        }
        sb.append("]}]");
        return sb.toString();
    }
}
