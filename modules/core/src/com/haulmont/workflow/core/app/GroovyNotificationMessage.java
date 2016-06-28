/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import groovy.lang.Binding;

import java.util.Map;

public class GroovyNotificationMessage implements NotificationMessage {
    private Binding binding;
    private Map<String,Object> parameters;
    private String script;

    public GroovyNotificationMessage(String script){
        this.script = script;
    }

    @Override
    public String getSubject() {
        String subject = (String)binding.getVariable("subject");
        return subject;
    }

    @Override
    public String getBody() {
        String body = (String)binding.getVariable("body");
        return body;
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        this.parameters=parameters;
        binding = new Binding(parameters);
        AppBeans.get(Scripting.class).evaluateGroovy(script, binding);

    }
}