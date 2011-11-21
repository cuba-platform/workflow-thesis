/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.exception;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.exception.AbstractExceptionHandler;
import com.haulmont.workflow.core.exception.WorkflowException;
import com.vaadin.ui.Window;

import javax.annotation.Nullable;

/**
 * Handles {@link WorkflowException}.
 *
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class WorkflowExceptionHandler extends AbstractExceptionHandler {

    public WorkflowExceptionHandler() {
        super(WorkflowException.class.getName());
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        if (throwable != null && throwable instanceof WorkflowException) {
            WorkflowException e = (WorkflowException) throwable;
            if (e.getType().equals(WorkflowException.Type.NO_ACTIVE_EXECUTION)) {
                String msg = MessageProvider.getMessage(getClass(), "WorkflowException.noExecution");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
                return;
            }
            if (e.getType().equals(WorkflowException.Type.NO_CARD_ROLE)) {
                String msg = MessageProvider.getMessage(getClass(), "WorkflowException.noCardRole");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
                return;
            }
        }
        String msg = MessageProvider.getMessage(getClass(), "WorkflowException.undefined");
        app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
    }
}
