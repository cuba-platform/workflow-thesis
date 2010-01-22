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
        switch (e.getType()) {
            case NO_ACTIVE_EXECUTION:
                String msg = MessageProvider.getMessage(getClass(), "WorkflowException.noExecution");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
                break;
            case NO_CARD_ROLE:
                msg = MessageProvider.getMessage(getClass(), "WorkflowException.noCardRole");
                if (e.getParams().length > 0)
                    msg = String.format(msg, e.getParams());
                app.getAppWindow().showNotification(msg, Window.Notification.TYPE_ERROR_MESSAGE);
                break;
            default:
                throw e;
        }
    }
}
