package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.ScriptingProvider;
import groovy.lang.Binding;

import java.util.Map;

/*
* Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
* Haulmont Technology proprietary and confidential.
* Use is subject to license terms.

* Author: Konstantin Devyatkin
*
*
* $Id
*/
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
        ScriptingProvider.evaluateGroovy(ScriptingProvider.Layer.CORE, script, binding);

    }
}
