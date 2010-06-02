/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 27.05.2010 15:25:02
 *
 * $Id$
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
}
