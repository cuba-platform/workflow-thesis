/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.01.2010 13:41:40
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.*;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import com.haulmont.workflow.core.global.TimeUnit;

import java.util.Calendar;
import java.util.Date;

public class WorkCalendarTest extends WfTestCase {

    private WorkCalendarAPI workCalendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workCalendar = Locator.lookup(WorkCalendarAPI.NAME);
    }

    @Override
    protected void tearDown() throws Exception {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createNativeQuery("delete from WF_CALENDAR");
            q.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
        super.tearDown();
    }

    public void test() {
        createStandardWorkTime();

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, 2010);
        cal.set(Calendar.MONTH, 0);
        cal.set(Calendar.DAY_OF_MONTH, 10);
        cal.set(Calendar.HOUR, 10);
        cal.set(Calendar.MINUTE, 10);

//        Long millis = workCalendar.getAbsoluteMillis(cal.getTime(), 10, TimeUnit.MINUTE);
//        assertEquals(600000L, millis.longValue());
        Calendar startDay = Calendar.getInstance();
        startDay.set(Calendar.YEAR, 2010);
        startDay.set(Calendar.MONTH, 1);
        startDay.set(Calendar.DAY_OF_MONTH, 6);
        startDay.set(Calendar.HOUR_OF_DAY, 10);
        startDay.set(Calendar.MINUTE, 10);

        Date endDate = workCalendar.addInterval(startDay.getTime(), 5, TimeUnit.HOUR);
        Calendar endDay = Calendar.getInstance();
        endDay.setTime(endDate);
        assertEquals(endDay.get(Calendar.MONTH), 1);
        assertEquals(endDay.get(Calendar.DAY_OF_MONTH), 8);
        assertEquals(endDay.get(Calendar.HOUR_OF_DAY), 15);
        assertEquals(endDay.get(Calendar.MINUTE), 0);

//        Long workDayLength = workCalendar.getWorkDayLengthInMillis();
//        assertEquals(2880000L, workDayLength.longValue());

    }

    private void createStandardWorkTime() {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();

            em.persist(createWorkCalendarEntity(null, "0900", "1300"));
            em.persist(createWorkCalendarEntity(null, "1400", "1800"));

            Calendar calendar = Calendar.getInstance();
            calendar.set(2010, 01, 05);
            em.persist(createWorkCalendarEntity(calendar.getTime(), "0900", "1300"));
            em.persist(createWorkCalendarEntity(calendar.getTime(), "1400", "1700"));

            calendar.set(2010, 01, 06);
            em.persist(createWorkCalendarEntity(calendar.getTime(), null, null));
            
            calendar.set(2010, 01, 07);
            em.persist(createWorkCalendarEntity(calendar.getTime(), null, null));

            tx.commit();
        } finally {
            tx.end();
        }
    }

    private WorkCalendarEntity createWorkCalendarEntity(Date day, String start, String end) {
        WorkCalendarEntity calendarEntity = new WorkCalendarEntity();
        calendarEntity.setDay(day);
        calendarEntity.setStart(start);
        calendarEntity.setEnd(end);

        return calendarEntity;
    }
    
}
