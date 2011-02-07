/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.12.10 14:58
 *
 * $Id$
 */
package com.haulmont.workflow.web.wfdesigner;

import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.sys.StaticContentServlet;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class StaticServlet extends StaticContentServlet {

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

        public void respondGet(HttpServletResponse resp) throws IOException {
            setHeaders(resp);

            Configuration configuration = getFreemarkerConfiguration();
            WebappTemplateLoader loader = new WebappTemplateLoader(getServletContext());
            configuration.setTemplateLoader(loader);
            Template template = configuration.getTemplate(getPath(req));

            try {
                template.process(createTemplateParams(), resp.getWriter());
            } catch (TemplateException e) {
                throw new RuntimeException(e);
            }
        }

        private Map<String, Object> createTemplateParams() {
            HashMap<String, Object> params = new HashMap<String, Object>();
            UserSession userSession = (UserSession) req.getSession().getAttribute(App.USER_SESSION_ATTR);
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

        public void respondHead(HttpServletResponse resp) {
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
}
