/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Devyatkin
 * Created: ${DATE} ${TIME}
 *
 * $Id$
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
    private Map<String, Object> dataModel;
    private Template templateSubject;
    private Template templateBody;
    private String subject;
    private String body;

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

    public String getSubject() {
        return subject;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getBody() {
        return body;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setParameters(Map<String, Object> parameters) {
        this.dataModel = parameters;
        StringWriter outSubject = new StringWriter(256);
        StringWriter outBody = new StringWriter(2048);
        try {
            templateSubject.process(dataModel, outSubject);
            subject = outSubject.getBuffer().toString();

            templateBody.process(dataModel, outBody);
            body = outBody.getBuffer().toString();

        } catch (TemplateException e) {
            log.warn("Can not load templeate");
        } catch (IOException e) {
            log.warn("Can not load templeate");
        }
    }
}
