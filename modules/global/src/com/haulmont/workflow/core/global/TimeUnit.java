/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.global;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum TimeUnit implements EnumClass<String>{

    MINUTE(60000L, "M"),
    HOUR(3600000L, "H"),
    DAY(86400000L, "D");

    private final long millis;
    private String id;

    TimeUnit(long millis, String id) {
        this.millis = millis;
        this.id = id;
    }

    public long getMillis() {
        return millis;
    }

    public static TimeUnit fromId(String id) {
        if ("M".equals(id))
            return MINUTE;
        else if ("H".equals(id))
            return HOUR;
        else if ("D".equals(id))
            return DAY;
        else
            return null;
    }

    @Override
    public String getId() {
        return id;
    }
}