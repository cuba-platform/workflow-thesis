/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.12.10 16:33
 *
 * $Id$
 */
package com.haulmont.workflow.web.wfdesigner;

import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.sys.WebSecurityUtils;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** @deprecated Need to use {@link ActionController} */
public class ActionServlet extends HttpServlet {

    private static final long serialVersionUID = 579462138595504996L;

    private Log log = LogFactory.getLog(ActionServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String idStr = req.getParameter("id");
            if (idStr == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No design ID provided");
                return;
            }

            UserSession userSession = (UserSession) req.getSession().getAttribute(App.USER_SESSION_ATTR);
            if (userSession == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            WebSecurityUtils.setSecurityAssociation(userSession.getUser().getLogin(), userSession.getId());
            try {
                UUID designId = UUID.fromString(idStr);
                if ("/load.json".equals(req.getPathInfo())) {
                    Design design = loadDesign(designId);
                    if (design == null) {
                        resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Design not found");
                        return;
                    }

                    String src = design.getSrc() == null ? "" : design.getSrc();

                    setHeaders(resp);
                    PrintWriter out = resp.getWriter();
                    out.println("[" + src + "]");
                    out.close();
                } else if ("/loadScripts.json".equals(req.getPathInfo())) {
                    List<DesignScript> list = loadDesignScripts(designId);

                    StringBuilder sb = new StringBuilder("[");
                    for (Iterator<DesignScript> it = list.iterator(); it.hasNext();) {
                        final DesignScript designScript = it.next();
                        sb.append("\"").append(designScript.getName()).append("\"");
                        if (it.hasNext())
                            sb.append(",");
                    }
                    sb.append("]");

                    setHeaders(resp);
                    PrintWriter out = resp.getWriter();
                    out.println(sb.toString());
                    out.close();
                }
            } finally {
                WebSecurityUtils.clearSecurityAssociation();
            }
        } catch (IOException e) {
            log.error("Error processing GET", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Error processing GET", e);
            throw e;
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String idStr = req.getParameter("id");
            if (idStr == null) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "No design ID provided");
                return;
            }

            UserSession userSession = (UserSession) req.getSession().getAttribute(App.USER_SESSION_ATTR);
            if (userSession == null) {
                resp.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            String src = IOUtils.toString(req.getInputStream(), "UTF-8");
            JSONObject jsonObject = new JSONObject(src);
            String name = jsonObject.getString("name");

            WebSecurityUtils.setSecurityAssociation(userSession.getUser().getLogin(), userSession.getId());
            try {
                UUID designId = UUID.fromString(idStr);
                Design design = loadDesign(designId);
                if (design == null) {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Design not found");
                    return;
                }

                design.setName(name);
                design.setSrc(src);
                design.setCompileTs(null);

                CommitContext commitContext = new CommitContext(Collections.singleton(design));
                ServiceLocator.getDataService().commit(commitContext);

                setHeaders(resp);
                PrintWriter out = resp.getWriter();
                out.println("{\"error\": null}");
                out.close();
            } finally {
                WebSecurityUtils.clearSecurityAssociation();
            }
        } catch (JSONException e) {
            log.error("Error processing POST", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            log.error("Error processing POST", e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Error processing POST", e);
            throw e;
        }
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
