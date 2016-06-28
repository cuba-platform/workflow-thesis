/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.enums;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

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

    @Override
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