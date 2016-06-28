/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.enums;

import com.haulmont.chile.core.datatypes.impl.EnumClass;

public enum SmsStatus implements EnumClass<Integer> {
    IN_QUEUE(0),
    DELIVERED(100),
    NON_DELIVERED(200),
    LOST_NOTIFICATION(300),
    BUFFERED_SMSC(400),
    ACCEPTD(500),
    EXPIRED(600),
    DELETED(700),
    UNKNOWN_STATUS(800),
    REJECTED(900),
    ERROR(1000);

    private Integer id;

    SmsStatus(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public static SmsStatus fromId(Integer id) {
        for (SmsStatus ss : SmsStatus.values()) {
            if (ss.getId().equals(id)) {
                return ss;
            }
        }
        return null;
    }

    public static SmsStatus fromString(String str) {
        for (SmsStatus ss : SmsStatus.values()) {
            if (ss.name().replace("_", " ").equalsIgnoreCase(str)) {
                return ss;
            }
        }
        return null;
    }
}
