/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
public enum DesignType implements EnumClass<String> {
    COMMON("COMMON"),
    SUBDESIGN("SUBDESIGN");

    private String id;

    DesignType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static DesignType fromId(String id) {
        for (DesignType type : DesignType.values()) {
            if ((type.getId()).equals(id)) {
                return type;
            }
        }
        return null;
    }
}
