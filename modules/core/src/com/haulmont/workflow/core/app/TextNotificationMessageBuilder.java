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

import java.util.Map;


public class TextNotificationMessageBuilder implements NotificationMessageBuilder {

    private String subject;
    private String body;

    public TextNotificationMessageBuilder(String text) {
        //     this.text=text;
        int subjectStart = text.indexOf("<!--subject-->") + "<!--subject-->".length();
        int subjectEnd = text.indexOf("<!--subject-end-->");
        if ((subjectStart >= "<!--subject-->".length()) && (subjectEnd > subjectStart)) {
            subject = text.substring(subjectStart, subjectEnd);
        } else {
            subject = "Error in template.Check subject tags";
        }

        int bodyStart = text.indexOf("<!--body-->") + "<!--body-->".length();
        int bodyEnd = text.indexOf("<!--body-end-->");
        if ((bodyStart >= "<!--body-->".length()) && (bodyEnd > bodyStart)) {
            body = text.substring(bodyStart, bodyEnd);
        } else {
            body = "Error in template.Check body tags";
        }

    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public void setParameters(Map<String, Object> parameters) {

    }
}