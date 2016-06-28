/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.Frame;

import java.util.Map;
import java.util.concurrent.Callable;

public class NotificationForm implements Callable<Boolean> {

    protected String message;

    public NotificationForm(Map<String, String> params) {
        message = params.get("message") == null ? "Fix parameters passing!" : params.get("message");
    }

    @Override
    public Boolean call() throws Exception {
        AppBeans.get(WindowManagerProvider.class).get().showNotification(message, Frame.NotificationType.HUMANIZED);
        return null;
    }
}