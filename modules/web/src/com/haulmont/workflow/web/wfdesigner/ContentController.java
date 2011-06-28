/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * Author: Nikolay Gorodnov
 * Created: 11.03.2011 11:23:29
 *
 * $Id$
 */
package com.haulmont.workflow.web.wfdesigner;

import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.cuba.web.controllers.StaticContentController;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/wfdesigner/**")
public class ContentController extends StaticContentController {
    public class FreemarkerTemplateFile implements LookupResult {

        protected long lastModified;
        protected String mimeType;
        private HttpServletRequest req;

        protected volatile Configuration configuration;

        public FreemarkerTemplateFile(long lastModified, String mimeType, HttpServletRequest req) {
            this.lastModified = lastModified;
            this.mimeType = mimeType;
            this.req = req;
        }

        public void respondGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            setHeaders(resp);

            Configuration configuration = getFreemarkerConfiguration();
            WebappTemplateLoader loader = new WebappTemplateLoader(req.getSession().getServletContext());
            configuration.setTemplateLoader(loader);
            Template template = configuration.getTemplate(getPath(this.req));

            try {
                template.process(createTemplateParams(), resp.getWriter());
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
        }

        private Map<String, Object> createTemplateParams() {
            HashMap<String, Object> params = new HashMap<String, Object>();
            UserSession userSession = ControllerUtils.getUserSession(req);
            params.put("locale", userSession == null ? "en" : userSession.getLocale().toString());
            return params;
        }

        private Configuration getFreemarkerConfiguration() {
            if (configuration == null) {
                synchronized (this) {
                    if (configuration == null) {
                        configuration = new Configuration();
                    }
                }
            }
            return configuration;
        }

        public void respondHead(HttpServletRequest req, HttpServletResponse resp) {
        }

        public long getLastModified() {
            return -1;
        }

        protected void setHeaders(HttpServletResponse resp) {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(mimeType);
            resp.setCharacterEncoding("UTF-8");
        }
    }

    @Override
    public String handleGetRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handleGetRequest(request, response);
        }
        return null;
    }

    @Override
    public String handlePostRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handlePostRequest(request, response);
        }
        return null;
    }

    @Override
    public String handleHeadRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (checkUserSession(request, response)) {
            return super.handleHeadRequest(request, response);
        }
        return null;
    }

    @Override
    protected String getMimeType(String path) {
        if (path.toLowerCase().endsWith(".ftl"))
            return "text/html";
        else
            return super.getMimeType(path);
    }

    @Override
    protected LookupResult createLookupResult(
            HttpServletRequest req, long lastModified, String mimeType, int contentLength, boolean acceptsDeflate, URL url)
    {
        if (req.getPathInfo().toLowerCase().endsWith(".ftl"))
            return new FreemarkerTemplateFile(lastModified, mimeType, req);
        else
            return super.createLookupResult(req, lastModified, mimeType, contentLength, acceptsDeflate, url);
    }

    protected boolean checkUserSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        UserSession userSession = ControllerUtils.getUserSession(request);
        if (userSession != null) {
            return true;
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }
}
