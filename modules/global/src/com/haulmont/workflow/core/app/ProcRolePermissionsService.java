/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.ProcRole;
import com.haulmont.workflow.core.global.ProcRolePermissionType;

public interface ProcRolePermissionsService {
    String NAME = "workflow_procRolePermissionsService";

    /**
     * Checks if specified card role is permitted for a given process state.
     *
     * @param cardRoleTo card role
     * @param state      process state
     * @param type       permission type
     * @return is card role permitted
     */
    boolean isPermitted(CardRole cardRoleTo, String state, ProcRolePermissionType type);

    /**
     * Checks if a specified process role is permitted for a given process state.
     *
     * @param card       card in a workflow process
     * @param procRoleTo process role
     * @param state      process state
     * @param type       permission type
     * @return is card role permitted
     */
    boolean isPermitted(Card card, ProcRole procRoleTo, String state, ProcRolePermissionType type);

    /**
     * Clears process roles permission cache.
     */
    void clearPermissionsCache();
}
