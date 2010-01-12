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

import com.haulmont.cuba.web.App;
import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.workflow.web.exception.WorkflowExceptionHandler;

public class WorkflowApp extends App {

    @Override
    protected void deployViews() {
        super.deployViews();
        MetadataProvider.getViewRepository().deployViews("/com/haulmont/workflow/web/workflow.views.xml");
    }

    @Override
    protected void initExceptionHandlers(boolean isConnected) {
        super.initExceptionHandlers(isConnected);
        if (isConnected) {
            exceptionHandlers.addHandler(new WorkflowExceptionHandler());
        }
    }
}
