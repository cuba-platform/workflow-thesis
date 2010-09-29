/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 19.02.2010 12:35:37
 *
 * $Id$
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
}
