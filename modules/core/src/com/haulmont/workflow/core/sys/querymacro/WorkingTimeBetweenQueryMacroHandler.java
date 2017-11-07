/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.sys.querymacro;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.sys.querymacro.TimeBetweenQueryMacroHandler;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.global.TimeUnit;
import org.apache.commons.lang.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class WorkingTimeBetweenQueryMacroHandler extends TimeBetweenQueryMacroHandler {

    static {
        units.put("workday", TimeUnit.DAY);
        units.put("workhour", TimeUnit.HOUR);
        units.put("workminute", TimeUnit.MINUTE);
    }

    @Override
    protected Date computeDate(int num, String unit) {
        TimeSource timeSource = AppBeans.get(TimeSource.class);
        Object timeUnit = units.get(unit.toLowerCase());
        if (timeUnit instanceof TimeUnit) {
            Date date;
            if (num == 0) {
                date = timeSource.currentTimestamp();
            } else {
                WorkCalendarAPI wc = AppBeans.get(WorkCalendarAPI.NAME);
                date = wc.addInterval(timeSource.currentTimestamp(), num, (TimeUnit) timeUnit);
            }
            switch ((TimeUnit) timeUnit) {
                case DAY: return DateUtils.truncate(date, Calendar.DAY_OF_MONTH);
                case HOUR: return DateUtils.truncate(date, Calendar.HOUR);
                case MINUTE: return DateUtils.truncate(date, Calendar.MINUTE);
                default: throw new UnsupportedOperationException("Unsupported TimeUnit: " + timeUnit);
            }
        } else {
            return super.computeDate(num, unit);
        }
    }
}
