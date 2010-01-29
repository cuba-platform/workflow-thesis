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

    MINUTE(60000L),
    HOUR(3600000L),
    DAY(86400000L);

    private final long millis;

    TimeUnit(long millis) {
        this.millis = millis;
    }

    public long getMillis() {
        return millis;
    }
}
