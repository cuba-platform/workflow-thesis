/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
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
        workCalendar = AppBeans.get(WorkCalendarAPI.NAME);
    }

//    @Override
//    protected void tearDown() throws Exception {
//        Transaction tx = Locator.createTransaction();
//        try {
//            EntityManager em = persistence.getEntityManager();
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
//            Date endDate = workCalendarCache.addInterval(df.parse("25.01.2011 11:00"), 5, TimeUnit.MINUTE);
//            assertEquals(df.parse("25.01.2011 11:05"), endDate);
//
//            endDate = workCalendarCache.addInterval(df.parse("28.01.2011 22:00"), 2, TimeUnit.HOUR);
//            assertEquals(df.parse("31.01.2011 11:00"), endDate);
//
//            endDate = workCalendarCache.addInterval(df.parse("25.01.2011 19:00"), 2, TimeUnit.DAY);
//            assertEquals(df.parse("27.01.2011 18:00"), endDate);
//
//            endDate = workCalendarCache.addInterval(df.parse("25.01.2011 21:00"), 15, TimeUnit.HOUR);
//            assertEquals(df.parse("27.01.2011 17:00"), endDate);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }

    public void testAddInterval() throws ParseException {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date;
        date = workCalendar.addInterval(df.parse("22.10.2013 15:00"), 1, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 16:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 15:00"), 4, TimeUnit.HOUR);
        assertEquals(df.parse("23.10.2013 10:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 13:10"), 1, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 15:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 12:10"), 1, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 14:10"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 09:00"), 7, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 17:00"), date);

        date = workCalendar.addInterval(df.parse("25.10.2013 17:00"), 2, TimeUnit.HOUR);
        assertEquals(df.parse("28.10.2013 10:00"), date);

        date = workCalendar.addInterval(df.parse("26.10.2013 17:00"), 1, TimeUnit.HOUR);
        assertEquals(df.parse("28.10.2013 10:00"), date);

        date = workCalendar.addInterval(df.parse("26.10.2013 17:00"), 9, TimeUnit.HOUR);
        assertEquals(df.parse("29.10.2013 10:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 15:00"), -1, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 14:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 15:00"), -2, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 12:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 15:00"), -2, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 12:00"), date);

        date = workCalendar.addInterval(df.parse("27.10.2013 15:00"), -2, TimeUnit.HOUR);
        assertEquals(df.parse("25.10.2013 16:00"), date);

        date = workCalendar.addInterval(df.parse("27.10.2013 15:00"), -6, TimeUnit.HOUR);
        assertEquals(df.parse("25.10.2013 11:00"), date);

        date = workCalendar.addInterval(df.parse("22.10.2013 13:10"), -1, TimeUnit.HOUR);
        assertEquals(df.parse("22.10.2013 12:00"), date);

        date = workCalendar.addInterval(df.parse("08.03.2013 13:10"), 1, TimeUnit.HOUR);
        assertEquals(df.parse("11.03.2013 10:00"), date);
    }

    public void testIntervalDurationCalc() {
        DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        try {
//            double duration = workCalendarCache.getIntervalDuration(df.parse("21.02.2011 11:00"), df.parse("22.02.2011 11:00"), TimeUnit.DAY);
//            assertEquals(1.0, duration);

//            duration = workCalendarCache.getIntervalDuration(df.parse("20.02.2011 11:00"), df.parse("22.02.2011 11:00"), TimeUnit.DAY);
//            assertEquals(1.0, duration);

            double duration = 0.0;
            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 11:00"), df.parse("25.03.2011 16:00"), TimeUnit.HOUR);
            assertEquals(4.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 14:00"), df.parse("25.03.2011 16:00"), TimeUnit.HOUR);
            assertEquals(2.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 12:00"), df.parse("25.03.2011 14:00"), TimeUnit.HOUR);
            assertEquals(1.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("26.03.2011 13:00"), df.parse("27.03.2011 13:10"), TimeUnit.HOUR);
            assertEquals(0.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 13:00"), df.parse("25.03.2011 13:10"), TimeUnit.HOUR);
            assertEquals(0.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 16:00"), df.parse("28.03.2011 10:00"), TimeUnit.HOUR);
            assertEquals(3.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("20.02.2011 19:00"), df.parse("22.02.2011 21:00"), TimeUnit.HOUR);
            assertEquals(16.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("25.03.2011 16:00"), df.parse("27.03.2011 21:00"), TimeUnit.HOUR);
            assertEquals(2.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("26.03.2011 16:00"), df.parse("27.03.2011 21:00"), TimeUnit.HOUR);
            assertEquals(0.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("21.03.2011 18:04"), df.parse("21.03.2011 18:20"), TimeUnit.HOUR);
            assertEquals(0.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("03.02.2011 17:00"), df.parse("03.02.2011 18:26"), TimeUnit.HOUR);
            assertEquals(1.0, duration);

            duration = workCalendar.getIntervalDuration(df.parse("20.02.2011 11:00"), df.parse("22.02.2011 06:00"), TimeUnit.HOUR);
            assertEquals(8.0, duration);

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
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            em.persist(createWorkCalendarEntity(null, "09:00", "13:00"));
            em.persist(createWorkCalendarEntity(null, "14:00", "18:00"));

            Calendar calendar = Calendar.getInstance();
            calendar.set(2010, 01, 05);
            em.persist(createWorkCalendarEntity(calendar.getTime(), "09:00", "13:00"));
            em.persist(createWorkCalendarEntity(calendar.getTime(), "14:00", "17:00"));

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
        DateFormat format = new SimpleDateFormat("hh:mm");
        WorkCalendarEntity calendarEntity = new WorkCalendarEntity();
        calendarEntity.setDay(day);
        try {

        if (start!=null)
                calendarEntity.setStart(format.parse(start));

        if (end!=null)
            calendarEntity.setEnd(format.parse(end));
        } catch (ParseException e) {
                throw new RuntimeException(e);
        }
        return calendarEntity;
    }


    @Override
    protected void initDataSources() throws Exception {
        Class.forName("org.postgresql.Driver");
        TestDataSource ds = new TestDataSource("jdbc:postgresql://localhost/refapp", "root", "root");
        TestContext.getInstance().bind("java:comp/env/jdbc/CubaDS", ds);
    }
}
