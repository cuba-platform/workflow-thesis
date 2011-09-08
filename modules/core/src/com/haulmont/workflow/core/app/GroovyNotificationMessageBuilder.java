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

import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.ScriptingProvider;
import groovy.lang.Binding;

import java.util.Map;


public class GroovyNotificationMessageBuilder implements NotificationMessageBuilder {
    private Binding binding;
    private String script;

    public GroovyNotificationMessageBuilder(String script) {
        this.script = script;
    }

    public String getSubject() {
        String subject = binding.getVariable("subject").toString();
        return subject;
    }

    public String getBody() {
        String body = binding.getVariable("body").toString();
        return body;
    }

    public void setParameters(Map<String, Object> parameters) {

        binding = new Binding(parameters);
        ScriptingProvider.evaluateGroovy(Scripting.Layer.CORE, script, binding);
    }
}
