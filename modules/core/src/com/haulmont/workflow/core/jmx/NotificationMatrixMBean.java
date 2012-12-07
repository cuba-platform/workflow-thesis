/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 30.06.2010 17:22:40
 *
 * $Id$
 */
package com.haulmont.workflow.core.jmx;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface NotificationMatrixMBean {

    @ManagedOperationParameters({@ManagedOperationParameter(name = "processPath", description = "")})
    String reload(String processPath) throws Exception;
}
