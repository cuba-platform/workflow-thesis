/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.global;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum ProcRolePermissionValue implements EnumClass<Integer> {
    DENY(0),
    ALLOW(1);

    private Integer id;

    ProcRolePermissionValue(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static ProcRolePermissionValue fromId(Integer id) {
        if (id == null) return null;
        switch (id) {
            case 0: return DENY;
            case 1: return ALLOW;
        }
        return null;
    }

    public static ProcRolePermissionValue fromString(String string) {
        if (string == null) return null;

        if ("ALLOW".equals(string)) return ALLOW;
        else if ("DENY".equals(string)) return DENY;

        return null;
    }
}
