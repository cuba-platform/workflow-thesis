/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.ScriptingProvider;
import groovy.lang.Binding;

import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author shishov
 */
public class ScriptNotificationMessageBuilder implements NotificationMessageBuilder{
    private Binding binding;
    private String script;

    public ScriptNotificationMessageBuilder(String script) {
        this.script = script;
    }

    @Override
    public String getSubject() {
        return binding.getVariable("subject").toString();
    }

    @Override
    public String getBody() {
        return binding.getVariable("body").toString();
    }

    @Override
    public void setParameters(Map<String, Object> parameters) {
        binding = new Binding(parameters);
        String text = ScriptingProvider.getResourceAsString(script);
        ScriptingProvider.evaluateGroovy(text, binding);
    }
}
