/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.app.NotificationMatrixAPI;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author krivopustov
 * @version $Id$
 */
@ManagedBean("workflow_NotificationMatrixMBean")
public class NotificationMatrix implements NotificationMatrixMBean {

    @Inject
    protected NotificationMatrixAPI notificationMatrix;

    @Override
    public String reload(String processPath) {
        try {
            notificationMatrix.reload(processPath);
            return "Done";
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }
}
