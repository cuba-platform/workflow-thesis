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

import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.SecurityContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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

    @RequestMapping(method = RequestMethod.POST)
    public String handlePostRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "id", required = false) String id
    )  {
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
                    ServiceLocator.getDataService().commit(commitContext);

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
            throw new RuntimeException( t);
        }
    }

    @RequestMapping(method = RequestMethod.GET)
    public String handleGetRequest(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "id", required = false) String id
    ) {
        try {
            if (auth(request, response)) {
                try {
                    Design design;
                    if (request.getPathInfo().endsWith("/load.json") && (design = findDesign(request, response, id)) != null) {
                        String src = design.getSrc() == null ? "" : design.getSrc();
                        setHeaders(response);
                        PrintWriter out = response.getWriter();
                        out.println("[" + src + "]");
                        out.close();
                    } else if (request.getPathInfo().endsWith("/loadScripts.json")) {
                        List<DesignScript> designScripts = findDesignScripts(request, response, id);

                        StringBuilder sb = new StringBuilder("[");
                        for (Iterator<DesignScript> it = designScripts.iterator(); it.hasNext();) {
                            final DesignScript designScript = it.next();
                            sb.append("\"").append(designScript.getName()).append("\"");
                            if (it.hasNext())
                                sb.append(",");
                        }
                        sb.append("]");

                        setHeaders(response);
                        PrintWriter out = response.getWriter();
                        out.println(sb.toString());
                        out.close();
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
            throw new RuntimeException( t);
        }
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

    private Design loadDesign(UUID designId) {
        LoadContext ctx = new LoadContext(Design.class).setId(designId).setView("_local");
        return ServiceLocator.getDataService().load(ctx);
    }

    private List<DesignScript> loadDesignScripts(UUID designId) {
        LoadContext ctx = new LoadContext(DesignScript.class).setView("_minimal");
        ctx.setQueryString("select s from wf$DesignScript s where s.design.id = :designId").addParameter("designId", designId);
        return ServiceLocator.getDataService().loadList(ctx);
    }

    protected void setHeaders(HttpServletResponse resp) {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
