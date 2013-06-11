/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.enums;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

/**
 *
 * <p>$Id$</p>
 *
 * @author zaharchenko
 */
public enum AttributeType implements EnumClass<String> {
    STRING("STRING"),
    INTEGER("INTEGER"),
    DOUBLE("DOUBLE"),
    BOOLEAN("BOOLEAN"),
    DATE("DATE"),
    DATE_TIME("DATE_TIME"),
    ENTITY("ENTITY"),
    ENUM("ENUM");

    private String id;

    AttributeType(String value) {
        this.id = value;
    }

    public String getId() {
        return id;
    }

    public static AttributeType fromId(String id) {
        for (AttributeType at : AttributeType.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}
