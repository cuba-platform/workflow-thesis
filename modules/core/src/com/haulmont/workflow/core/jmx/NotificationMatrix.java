/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.app.NotificationMatrixAPI;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.ManagedBean;

/**
 * @author krivopustov
 * @version $Id$
 */
@ManagedBean("workflow_NotificationMatrixMBean")
public class NotificationMatrix implements NotificationMatrixMBean {

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
