/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 02.11.2010 13:38:03
 *
 * $Id$
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
