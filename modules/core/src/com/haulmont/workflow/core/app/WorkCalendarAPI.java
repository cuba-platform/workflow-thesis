/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.global.TimeUnit;

import java.util.Calendar;
import java.util.Date;

public interface WorkCalendarAPI {

    String NAME = "workflow_WorkCalendar";

    Long getAbsoluteMillis(Date startDate, int qty, TimeUnit unit);

    Date addInterval(Date startDate, int qty, TimeUnit unit);

    Long getWorkDayLengthInMillis();

    Long getWorkPeriodDurationInDays(Date startTime, Date endTime);

    boolean isDateWorkDay(Date date);

    boolean isDateWorkDay(Calendar date);

    boolean isTimeWorkTime(Date date);

    boolean isTimeWorkTime(Calendar date);

    Double getIntervalDuration(Date startTime, Date endTime, TimeUnit timeUnit);

    int getCacheSize();

    void invalidateCache();
}
