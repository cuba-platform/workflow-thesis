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
import com.haulmont.cuba.testsupport.TestContext;
import com.haulmont.cuba.testsupport.TestDataSource;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import com.haulmont.workflow.core.global.TimeUnit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class WorkCalendarTest extends WfTestCase {

    private WorkCalendarAPI workCalendar;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workCalendar = Locator.lookup(WorkCalendarAPI.NAME);
    }

//    @Override
//    protected void tearDown() throws Exception {
//        Transaction tx = Locator.createTransaction();
//        try {
//            EntityManager em = PersistenceProvider.getEntityManager();
//            Query q = em.createNativeQuery("delete from WF_CALENDAR");
//            q.executeUpdate();
//            tx.commit();
//        } finally {
//            tx.end();
//        }
//        super.tearDown();
//    }

//    public void test() {
//        DateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm");
//        try {
//            Date endDate = workCalendar.addInterval(df.parse("25.01.2011 11:00"), 5, TimeUnit.MINUTE);
//            assertEquals(df.parse("25.01.2011 11:05"), endDate);
//
//            endDate = workCalendar.addInterval(df.parse("28.01.2011 22:00"), 2, TimeUnit.HOUR);
//            assertEquals(df.parse("31.01.2011 11:00"), endDate);
//
//            endDate = workCalendar.addInterval(df.parse("25.01.2011 19:00"), 2, TimeUnit.DAY);
//            assertEquals(df.parse("27.01.2011 18:00"), endDate);
//
//            endDate = workCalendar.addInterval(df.parse("25.01.2011 21:00"), 15, TimeUnit.HOUR);
//            assertEquals(df.parse("27.01.2011 17:00"), endDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }

    public void testIntervalDurationCalc() {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy hh:mm");

        try {
//            double duration = workCalendar.getIntervalDuration(df.parse("21.02.2011 11:00"), df.parse("22.02.2011 11:00"), TimeUnit.DAY);
//            assertEquals(1.0, duration);

//            duration = workCalendar.getIntervalDuration(df.parse("20.02.2011 11:00"), df.parse("22.02.2011 11:00"), TimeUnit.DAY);
//            assertEquals(1.0, duration);

            double duration = workCalendar.getIntervalDuration(df.parse("03.02.2011 16:00"), df.parse("03.02.2011 17:26"), TimeUnit.HOUR);
            assertEquals(1.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("20.02.2011 11:00"), df.parse("22.02.2011 06:00"), TimeUnit.HOUR);
            assertEquals(8.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("20.02.2011 19:00"), df.parse("22.02.2011 21:00"), TimeUnit.HOUR);
            assertEquals(16.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("22.02.2011 09:00"), df.parse("22.02.2011 11:00"), TimeUnit.HOUR);
            assertEquals(2.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("22.02.2011 09:00"), df.parse("22.02.2011 14:00"), TimeUnit.HOUR);
            assertEquals(4.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.02.2011 16:00"), df.parse("28.02.2011 10:00"), TimeUnit.HOUR);
            assertEquals(3.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("19.02.2011 16:00"), df.parse("20.02.2011 10:00"), TimeUnit.HOUR);
            assertEquals(0.0, duration);

        } catch (ParseException e) {
            e.printStackTrace();
        }
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


    @Override
    protected void initDataSources() throws Exception {
        Class.forName("org.postgresql.Driver");
        TestDataSource ds = new TestDataSource("jdbc:postgresql://localhost/alt239", "root", "root");
        TestContext.getInstance().bind("java:comp/env/jdbc/CubaDS", ds);
    }
}
