/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.global.Scripting;
import groovy.lang.Binding;

import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author shishov
 */
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
