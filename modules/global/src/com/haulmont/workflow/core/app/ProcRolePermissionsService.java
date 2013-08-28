/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 27.05.2010 16:13:58
 *
 * $Id$
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
