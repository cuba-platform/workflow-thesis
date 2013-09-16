/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.ScriptingProvider;
import groovy.lang.Binding;

import java.util.Map;

public class GroovyNotificationMessage implements NotificationMessage {
    private Binding binding;
    private Map<String,Object> parameters;
    private String script;

    public GroovyNotificationMessage(String script){
        this.script = script;
    }

    public String getSubject() {
        String subject = (String)binding.getVariable("subject");
        return subject;
    }

    public String getBody() {
        String body = (String)binding.getVariable("body");
        return body;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters=parameters;
        binding = new Binding(parameters);
        ScriptingProvider.evaluateGroovy(script, binding);

    }
}
