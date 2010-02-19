/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 11:47:51
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.global.TimeUnit;

import java.util.Date;

public interface WorkCalendarAPI {

    String NAME = "workflow_WorkCalendar";

    Long getAbsoluteMillis(Date startDate, int qty, TimeUnit unit);

    Date addInterval(Date startDate, int qty, TimeUnit unit);

    Long getWorkDayLengthInMillis();
}
