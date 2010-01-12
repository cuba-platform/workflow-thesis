/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 08.01.2010 16:05:27
 *
 * $Id$
 */
package com.haulmont.workflow.web.exception;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.exception.AbstractExceptionHandler;
import com.haulmont.workflow.core.exception.WorkflowException;
import com.vaadin.ui.Window;

public class WorkflowExceptionHandler extends AbstractExceptionHandler<WorkflowException> {

    public WorkflowExceptionHandler() {
        super(WorkflowException.class);
    }

    @Override
    protected void doHandle(WorkflowException e, App app) {
        if (e.getMessage().contains("No active execution")) {
            String msg = MessageProvider.getMessage(getClass(), "WorkflowException.noExecution");
            app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
        } else {
            throw e;
        }
    }
}
