/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 11:47:36
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.workflow.core.global.TimeUnit;

import javax.annotation.ManagedBean;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ManagedBean(WorkCalendarAPI.NAME)
public class WorkCalendar implements WorkCalendarAPI, WorkCalendarMBean {

    private static class CalendarItem {

        private final Date day;
        private final int startH;
        private final int startM;
        private final int endH;
        private final int endM;

        private CalendarItem(Date day, String start, String end) {
            this.day = day;
            this.startH = start == null ? 0 : Integer.valueOf(start.substring(0, 2));
            this.startM = start == null ? 0 : Integer.valueOf(start.substring(2));
            this.endH = end == null ? 0 : Integer.valueOf(end.substring(0, 2));
            this.endM = end == null ? 0 : Integer.valueOf(end.substring(2));
        }

        public Date getDay() {
            return day;
        }

        public int getEndH() {
            return endH;
        }

        public int getEndM() {
            return endM;
        }

        public int getStartH() {
            return startH;
        }

        public int getStartM() {
            return startM;
        }
    }

    private volatile List<CalendarItem> cache;

    private void loadCache() {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = new ArrayList<CalendarItem>();
                    Transaction tx = Locator.createTransaction();
                    try {
                        EntityManager em = PersistenceProvider.getEntityManager();
                        Query q = em.createQuery("select c.day, c.start, c.end from wf$Calendar c");
                        List<Object[]> list = q.getResultList();
                        for (Object[] row : list) {
                            CalendarItem ci = new CalendarItem((Date) row[0], (String) row[1], (String) row[2]);
                            cache.add(ci);
                        }

                        tx.commit();
                    } finally {
                        tx.end();
                    }
                }
            }
        }
    }

    public int getCacheSize() {
        return cache == null ? 0 : cache.size();
    }

    public void invalidateCache() {
        cache = null;
    }

    public Long getAbsoluteMillis(Date date, int qty, TimeUnit unit) {
        loadCache();
        return qty * unit.getMillis();
    }

    public Date addInterval(Date date, int qty, TimeUnit unit) {
        loadCache();
        return new Date(date.getTime() + qty * unit.getMillis());
    }
}
