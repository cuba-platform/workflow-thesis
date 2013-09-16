/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.web.App;

import java.util.Map;
import java.util.concurrent.Callable;

public class NotificationForm implements Callable<Boolean> {

    protected String message;

    public NotificationForm(Map<String, String> params) {
        message = params.get("message") == null ? "Fix parameters passing!" : params.get("message");
    }

    public Boolean call() throws Exception {
        App.getInstance().getWindowManager().showNotification(message, IFrame.NotificationType.HUMANIZED);
        return null;
    }
}
