/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 16:47
 *
 * $Id$
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
