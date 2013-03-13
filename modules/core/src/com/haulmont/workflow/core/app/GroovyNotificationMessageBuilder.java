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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Scripting;
import groovy.lang.Binding;

import java.util.Map;


public class GroovyNotificationMessageBuilder implements NotificationMessageBuilder {
    private String script;

    public GroovyNotificationMessageBuilder(String script) {
        this.script = script;
    }

    public NotificationMatrixMessage build(Map<String, Object> parameters) {
        Binding binding = new Binding(parameters);
        AppBeans.get(Scripting.class).evaluateGroovy(script, binding);

        return new NotificationMatrixMessage(binding.getVariable("subject").toString(), binding.getVariable("body").toString());
    }
}
