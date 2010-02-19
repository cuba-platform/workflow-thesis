/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 12:15:55
 *
 * $Id$
 */
package com.haulmont.workflow.core.global;

public enum TimeUnit {

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

    public String getId() {
        return id;
    }
}
