/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 18:41:17
 *
 * $Id$
 */
package com.haulmont.workflow.web;

import com.haulmont.cuba.web.DefaultApp;
import com.haulmont.workflow.web.exception.WorkflowExceptionHandler;

public class WorkflowApp extends DefaultApp {

    @Override
    protected void initExceptionHandlers(boolean isConnected) {
        super.initExceptionHandlers(isConnected);
        if (isConnected) {
            exceptionHandlers.addHandler(new WorkflowExceptionHandler());
        }
    }
}
