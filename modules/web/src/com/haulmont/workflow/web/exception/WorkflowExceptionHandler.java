/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.exception;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.exception.AbstractExceptionHandler;
import com.haulmont.workflow.core.exception.WorkflowException;

import javax.annotation.Nullable;

/**
 * Handles {@link WorkflowException}.
 *
 * @author gorbunkov
 * @version $Id$
 */
public class WorkflowExceptionHandler extends AbstractExceptionHandler {

    public WorkflowExceptionHandler() {
        super(WorkflowException.class.getName());
    }

    @Override
    protected void doHandle(App app, String className, String message, @Nullable Throwable throwable) {
        Messages messages = AppBeans.get(Messages.class);

        if (throwable != null && throwable instanceof WorkflowException) {
            WorkflowException e = (WorkflowException) throwable;
            if (e.getType().equals(WorkflowException.Type.NO_ACTIVE_EXECUTION)) {
                String msg = messages.getMessage(getClass(), "WorkflowException.noExecution");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
                return;
            }
            if (e.getType().equals(WorkflowException.Type.NO_CARD_ROLE)) {
                String msg = messages.getMessage(getClass(), "WorkflowException.noCardRole");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
                return;
            }
        }
        String msg = messages.getMessage(getClass(), "WorkflowException.undefined");
        app.getWindowManager().showNotification(msg, Frame.NotificationType.ERROR);
    }
}