/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * Author: Nikolay Gorodnov
 * Created: 09.03.2011 17:18:26
 *
 * $Id$
 */
package com.haulmont.workflow.web.wfdesigner;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.entity.DesignType;
import com.haulmont.workflow.core.entity.Proc;
import org.apache.commons.io.IOUtils;
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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/wfdesigner/*/action/*.json")
public class ActionController {

    protected Log log = LogFactory.getLog(getClass());

    @Inject
    protected DataService dataService;

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


    private List<Design> loadSubDesigns() {
        LoadContext ctx = new LoadContext(Design.class).setView("load-subdesign");
        ctx.setQueryString("select d from wf$Design d where d.type=:type and d.compileTs is not null order by d.name").addParameter("type", DesignType.SUBDESIGN.getId());
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

    private List<Proc> findProcs() {
        View view = new View(Proc.class).addProperty("code").addProperty("name");
        LoadContext ctx = new LoadContext(Proc.class).setView(view);
        ctx.setQueryString("select p from wf$Proc p order by p.name");
        return dataService.loadList(ctx);
    }

    private Design loadDesign(UUID designId) {
        LoadContext ctx = new LoadContext(Design.class).setId(designId).setView("_local");
        return dataService.load(ctx);
    }

    private List<DesignScript> loadDesignScripts(UUID designId) {
        LoadContext ctx = new LoadContext(DesignScript.class).setView("_minimal");
        ctx.setQueryString("select s from wf$DesignScript s where s.design.id = :designId").addParameter("designId", designId);
        return dataService.loadList(ctx);
    }
}
