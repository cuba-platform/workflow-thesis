/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.global.TimeUnit;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service(WorkCalendarService.NAME)
public class WorkCalendarServiceBean implements WorkCalendarService{

    public Date addInterval(Date startDate, int qty, TimeUnit unit) {
        WorkCalendarAPI workCalendarAPI = Locator.lookup(WorkCalendarAPI.NAME);
        return workCalendarAPI.addInterval(startDate, qty, unit);
    }

    public Long getWorkDayLengthInMillis() {
        WorkCalendarAPI workCalendarAPI = Locator.lookup(WorkCalendarAPI.NAME);
        return workCalendarAPI.getWorkDayLengthInMillis();
    }

    public Double getIntervalDuration(Date startTime, Date endTime, TimeUnit timeUnit) {
        WorkCalendarAPI workCalendarAPI = Locator.lookup(WorkCalendarAPI.NAME);
        return workCalendarAPI.getIntervalDuration(startTime, endTime, timeUnit);
    }
}
