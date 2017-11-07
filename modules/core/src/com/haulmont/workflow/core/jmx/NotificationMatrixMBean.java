/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.jmx;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface NotificationMatrixMBean {

    @ManagedOperationParameters({@ManagedOperationParameter(name = "processPath", description = "")})
    String reload(String processPath) throws Exception;
}
