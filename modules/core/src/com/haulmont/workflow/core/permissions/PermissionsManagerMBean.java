/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 07.06.2010 17:27:41
 *
 * $Id$
 */
package com.haulmont.workflow.core.permissions;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface PermissionsManagerMBean {
    String NAME = "workflow_PermissionsManager";

    @ManagedOperationParameters({@ManagedOperationParameter(name = "procName", description = "")})
    String deployPermissions(String procName);
}
