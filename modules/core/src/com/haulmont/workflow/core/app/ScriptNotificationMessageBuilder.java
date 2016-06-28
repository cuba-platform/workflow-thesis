/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.global.Scripting;
import groovy.lang.Binding;

import java.util.Map;

public class ScriptNotificationMessageBuilder implements NotificationMessageBuilder {
    private String script;

    public ScriptNotificationMessageBuilder(String script) {
        this.script = script;
    }

    @Override
    public NotificationMatrixMessage build(Map<String, Object> parameters) {
        Binding binding = new Binding(parameters);
        String text = AppBeans.get(Resources.class).getResourceAsString(script);
        AppBeans.get(Scripting.class).evaluateGroovy(text, binding);

        return new NotificationMatrixMessage(binding.getVariable("subject").toString(), binding.getVariable("body").toString());
    }
}
