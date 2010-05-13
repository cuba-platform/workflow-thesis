/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.05.2010 17:33:16
 *
 * $Id$
 */
package com.haulmont.workflow.core.sys;

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.TimeBetweenQueryMacroHandler;
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
        Object timeUnit = units.get(unit.toLowerCase());
        if (timeUnit instanceof TimeUnit) {
            Date date;
            if (num == 0) {
                date = TimeProvider.currentTimestamp();
            } else {
                WorkCalendarAPI wc = Locator.lookup(WorkCalendarAPI.NAME);
                date = wc.addInterval(TimeProvider.currentTimestamp(), num, (TimeUnit) timeUnit);
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
