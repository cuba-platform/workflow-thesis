/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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

    public static ProcRolePermissionType fromString(String string) {
        if (string == null) return null;
        
        if ("ADD".equals(string)) return ADD;
        else if ("REMOVE".equals(string)) return REMOVE;
        else if ("MODIFY".equals(string)) return MODIFY;

        return null;
    }
}
