/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 27.05.2010 12:35:49
 *
 * $Id$
 */
package com.haulmont.workflow.core.global;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum ProcRolePermissionType implements EnumClass<Integer> {
    ADD(1),
    MODIFY(2),
    REMOVE(3);

    private Integer id;

    ProcRolePermissionType(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static ProcRolePermissionType fromId(Integer id) {
        if (id == null) return null;
        switch (id) {
            case 1: return ADD;
            case 2: return MODIFY;
            case 3: return REMOVE;
        }
        return null;
    }
}
