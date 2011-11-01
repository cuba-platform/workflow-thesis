/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

/**
 * <p>$Id$</p>
 *
 * @author chernov
 */
public enum OrderFillingType implements EnumClass<String> {
    PARALLEL("P"),
    SEQUENTIAL("S");

    private String id;

    OrderFillingType(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static OrderFillingType fromId(String id) {
        if ("P".equals(id))
            return PARALLEL;
        else if ("S".equals(id))
            return SEQUENTIAL;
        else
            return null;
    }
}