/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import java.io.Serializable;
import java.util.Map;

public class NotificationMatrixMessage implements Serializable {
    private String subject;
    private String body;

    public NotificationMatrixMessage(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public NotificationMatrixMessage() {
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public interface MessageGenerator extends Serializable {

        NotificationMatrixMessage generateMessage(Map<String, Object> parameters);
    }
}
