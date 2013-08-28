/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 12:14:35
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.global.TimeUnit;

import java.util.Date;

public interface WorkCalendarService {
    String NAME = "workflow_WorkCalendarService";

    /**
     * Adds a specified time interval to a given date, based on a work calendar schedule.
     *
     * @param startDate start date
     * @param qty       time interval
     * @param unit      time unit
     * @return calculated date
     */
    Date addInterval(Date startDate, int qty, TimeUnit unit);

    /**
     * Calculates a work day duration in ms, based on a work calendar schedule.
     *
     * @return duration in ms
     */
    Long getWorkDayLengthInMillis();

    /**
     * Calculates a duration between specified dates using TimeUnit measuring.
     *
     * @param startTime start time
     * @param endTime   end time
     * @param timeUnit  time unit
     * @return duration
     */
    Double getIntervalDuration(Date startTime, Date endTime, TimeUnit timeUnit);
}
