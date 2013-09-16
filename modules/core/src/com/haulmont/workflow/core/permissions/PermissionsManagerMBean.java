/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.permissions;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface PermissionsManagerMBean {
    String NAME = "workflow_PermissionsManager";

    @ManagedOperationParameters({@ManagedOperationParameter(name = "procName", description = "")})
    String deployPermissions(String procName);
}
