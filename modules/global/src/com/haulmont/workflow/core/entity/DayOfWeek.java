/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

public enum DayOfWeek {
    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNESDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

    private Integer id;

    DayOfWeek(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public static DayOfWeek fromId(Integer id) {
        if (id == null) return null;
        switch (id) {
            case 1 : return SUNDAY;
            case 2 : return MONDAY;
            case 3 : return TUESDAY;
            case 4 : return WEDNESDAY;
            case 5 : return THURSDAY;
            case 6 : return FRIDAY;
            case 7 : return SATURDAY;
            default: return null;
        }
    }
}
