/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;


import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.logging.*;


public class FreeMarkerNotificationMessageBuilder implements NotificationMessageBuilder {
    private Template templateSubject;
    private Template templateBody;

    private Log log = LogFactory.getLog(FreeMarkerNotificationMessageBuilder.class);

    public FreeMarkerNotificationMessageBuilder(String templateString) {

        StringTemplateLoader stringLoader = new StringTemplateLoader();
        int subjectStart = templateString.indexOf("<!--subject-->") + "<!--subject-->".length();
        int subjectEnd = templateString.indexOf("<!--subject-end-->");

        String subjectTemp;
        if ((subjectStart >= "<!--subject-->".length()) && (subjectEnd > subjectStart)) {
            subjectTemp = templateString.substring(subjectStart, subjectEnd);
        } else {
            subjectTemp = "Error in template.Check subject tags";
        }

        stringLoader.putTemplate("subject", subjectTemp);

        int bodyStart = templateString.indexOf("<!--body-->") + "<!--body-->".length();
        int bodyEnd = templateString.indexOf("<!--body-end-->");

        String bodyTemp;

        if ((bodyStart >= "<!--body-->".length()) && (bodyEnd > bodyStart)) {
            bodyTemp = templateString.substring(bodyStart, bodyEnd);
        } else {
            bodyTemp = "Error in template.Check body tags";
        }

        stringLoader.putTemplate("body", bodyTemp);

        Configuration cfg = new Configuration();
        cfg.setTemplateLoader(stringLoader);
        try {
            templateSubject = cfg.getTemplate("subject");
            templateBody = cfg.getTemplate("body");
        } catch (IOException e) {
            log.warn("Can not load templeate");
        }
    }

    public NotificationMatrixMessage build(Map<String, Object> parameters) {
        Map<String, Object> dataModel = parameters;
        NotificationMatrixMessage msg = new NotificationMatrixMessage();
        StringWriter outSubject = new StringWriter(256);
        StringWriter outBody = new StringWriter(2048);
        try {
            templateSubject.process(dataModel, outSubject);
            msg.setSubject(outSubject.getBuffer().toString());

            templateBody.process(dataModel, outBody);
            msg.setBody(outBody.getBuffer().toString());

        } catch (TemplateException e) {
            log.warn("Can not load templeate");
        } catch (IOException e) {
            log.warn("Can not load templeate");
        }

        return msg;
    }
}
