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

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ManagementBean;
import com.haulmont.cuba.core.app.ResourceRepositoryService;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.workflow.core.entity.DayOfWeek;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import com.haulmont.workflow.core.global.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.ManagedBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@ManagedBean(WorkCalendarAPI.NAME)
public class WorkCalendar extends ManagementBean implements WorkCalendarAPI, WorkCalendarMBean {

    private static class CalendarItem {

        private final Date day;
        private final int startH;
        private final int startM;
        private final int endH;
        private final int endM;
        private final Integer dayOfWeek;

        private CalendarItem(Date day, String start, String end) {
            this.day = day;
            if (StringUtils.isNotBlank(start)) {
                this.startH = Integer.valueOf(start.substring(0, 2));
                this.startM = Integer.valueOf(start.substring(3));
            } else {
                this.startH = 0;
                this.startM = 0;
            }

            if (StringUtils.isNotBlank(end)) {
                this.endH = Integer.valueOf(end.substring(0, 2));
                this.endM = Integer.valueOf(end.substring(3));
            } else {
                this.endH = 0;
                this.endM = 0;
            }
            this.dayOfWeek = null;
        }

        private CalendarItem(Integer dayOfWeek, String start, String end) {
            this.dayOfWeek = dayOfWeek;
            if (StringUtils.isNotBlank(start)) {
                this.startH = Integer.valueOf(start.substring(0, 2));
                this.startM = Integer.valueOf(start.substring(3));
            } else {
                this.startH = 0;
                this.startM = 0;
            }

            if (StringUtils.isNotBlank(end)) {
                this.endH = Integer.valueOf(end.substring(0, 2));
                this.endM = Integer.valueOf(end.substring(3));
            } else {
                this.endH = 0;
                this.endM = 0;
            }
            this.day = null;
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

        public Long getDurationFromStart(Date timeTo) {
            long timeToFragment = DateUtils.getFragmentInMilliseconds(timeTo, Calendar.DAY_OF_YEAR);
            Calendar startTime = Calendar.getInstance();
            startTime.clear();
            startTime.set(Calendar.HOUR_OF_DAY, startH);
            startTime.set(Calendar.MINUTE, startM);
            long startTimeFragment = DateUtils.getFragmentInMilliseconds(startTime, Calendar.DAY_OF_YEAR);
            return timeToFragment - startTimeFragment;
        }

        public Long getDurationToEnd(Date timeFrom) {
            long timeFromFragment = DateUtils.getFragmentInMilliseconds(timeFrom, Calendar.DAY_OF_YEAR);
            Calendar endTime = Calendar.getInstance();
            endTime.clear();
            endTime.set(Calendar.HOUR_OF_DAY, endH);
            endTime.set(Calendar.MINUTE, endM);
            long endTimeFragment = DateUtils.getFragmentInMilliseconds(endTime, Calendar.DAY_OF_YEAR);
            return endTimeFragment - timeFromFragment;
        }

        public boolean isDateInInterval(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            if ((hour > startH) && (hour < endH)) {
                return true;
            } else if (hour == startH) {
                if (minutes >= startM) return true;
                else return false;
            } else if (hour == endH) {
                if (minutes < endM) return true;
                else return false;
            }
            return false;
        }

        public Long getDuration() {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, startH);
            startTime.set(Calendar.MINUTE, startM);
            endTime.set(Calendar.HOUR_OF_DAY, endH);
            endTime.set(Calendar.MINUTE, endM);
            return endTime.getTimeInMillis() - startTime.getTimeInMillis();
        }


        public boolean isDateBeforeInterval(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            if ((hour < startH) || ((hour == startH) && (minutes < startM))) {
                return true;
            }
            return false;
        }

        public boolean isDateAfterInterval(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            if ((hour > endH) || ((hour == endH) && (minutes > endM))) {
                return true;
            }
            return false;
        }

    }

    private volatile Map<Date, List<CalendarItem>> exceptionDays;
    private volatile Map<Integer, List<CalendarItem>> defaultDays;

    private Calendar currentDay;
    private boolean moveForward = true;
    private Date startTime;
    private ListIterator<CalendarItem> ciIterator;

    private void loadCaches() {
        if (exceptionDays == null) {
            synchronized (this) {
                if (exceptionDays == null) {
                    exceptionDays = new HashMap<Date, List<CalendarItem>>();
                    Transaction tx = Locator.createTransaction();
                    try {
                        EntityManager em = PersistenceProvider.getEntityManager();
                        Query q = em.createQuery("select c.day, c.start, c.end from wf$Calendar c where c.day is not null " +
                                "order by c.day, c.start");
                        List<Object[]> list = q.getResultList();
                        for (Object[] row : list) {
                            Date date = (Date) row[0];
                            CalendarItem ci = new CalendarItem(date, (String) row[1], (String) row[2]);
                            List<CalendarItem> mapValue = exceptionDays.get(date);
                            if (mapValue == null)
                                mapValue = new LinkedList<CalendarItem>();
                            mapValue.add(ci);
                            exceptionDays.put(date, mapValue);
                        }

                        tx.commit();
                    } finally {
                        tx.end();
                    }
                }
            }
        }

        if (defaultDays == null) {
            defaultDays = new HashMap<Integer, List<CalendarItem>>();
            Transaction tx = Locator.createTransaction();
            try {
                EntityManager em = PersistenceProvider.getEntityManager();
                Query q = em.createQuery("select c.dayOfWeek, c.start, c.end from wf$Calendar c where c.dayOfWeek is not null " +
                        "order by c.dayOfWeek, c.start");
                List<Object[]> list = q.getResultList();
                for (Object[] row : list) {
                    Integer dayOfWeek = (Integer) row[0];
                    CalendarItem ci = new CalendarItem(dayOfWeek, (String) row[1], (String) row[2]);
                    List<CalendarItem> mapValue = defaultDays.get(dayOfWeek);
                    if (mapValue == null)
                        mapValue = new LinkedList<CalendarItem>();
                    mapValue.add(ci);
                    defaultDays.put(dayOfWeek, mapValue);
                }
                tx.commit();
            } finally {
                tx.end();
            }
        }
    }

    public int getCacheSize() {
        return exceptionDays == null ? 0 : exceptionDays.size();
    }

    public void invalidateCache() {
        exceptionDays = null;
        defaultDays = null;
    }

    public Long getAbsoluteMillis(Date date, int qty, TimeUnit unit) {
        loadCaches();
        Date endDate = addInterval(date, qty, unit);
        return endDate.getTime() - date.getTime();
    }

    public Long getWorkDayLengthInMillis() {
        loadCaches();
        Long workDayLength = new Long(0);
        String defaultWorkDayProp = AppContext.getProperty("workflow.workCalendar.defaultWorkDay");
        Integer defaultWorkDay = defaultWorkDayProp == null ? Calendar.MONDAY : Integer.parseInt(defaultWorkDayProp);

        List<CalendarItem> defaultItems = defaultDays.get(defaultWorkDay);
        if (defaultItems != null) {
            for (CalendarItem calendarItem : defaultItems) {
                if (calendarItem.getDay() == null) {
                    workDayLength += calendarItem.getDuration();
                }
            }
        }
        return workDayLength;
    }


    //Test data filling
    public String fillWorkCalendar() {
        Transaction tx = Locator.createTransaction();
        try {
            login();
            deleteWorkCalendar();
            EntityManager em = PersistenceProvider.getEntityManager();

            String calendarResourceName = AppContext.getProperty("workflow.workCalendar.path");
            if (calendarResourceName == null) return "workflow.workCalendar.path property isn't set";
            ResourceRepositoryService rr = Locator.lookup(ResourceRepositoryService.NAME);
            if (rr.resourceExists(calendarResourceName)) {
                String xml = rr.getResAsString(calendarResourceName);
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();

                List<Date> filledDays = new ArrayList<Date>();

                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                for (Element dayElement : Dom4j.elements(root)) {
                    if ("workDay".equals(dayElement.getName())) {
                        em.persist(createWorkCalendarEntity(null, dayElement.attributeValue("start"), dayElement.attributeValue("end"),
                                Integer.parseInt(dayElement.attributeValue("dayOfWeek"))));
                    } else if ("dayOff".equals(dayElement.getName())) {
                        Date date = df.parse(dayElement.attributeValue("date"));
                        em.persist(createWorkCalendarEntity(date, null, null, null));
                        filledDays.add(date);
                    } else if ("exceptionDay".equals(dayElement.getName())) {
                        Date date = df.parse(dayElement.attributeValue("date"));
                        em.persist(createWorkCalendarEntity(date, dayElement.attributeValue("start"), dayElement.attributeValue("end"), null));
                        filledDays.add(date);
                    }
                }

            }

            tx.commit();
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
            logout();
        }
        return "Work calendar records created successfuly";
    }

    private void deleteWorkCalendar() throws Exception {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();

            em.setSoftDeletion(false);
            Query attrQuery = em.createQuery("select c from wf$Calendar c");
            List<WorkCalendarEntity> calendars = attrQuery.getResultList();
            for (WorkCalendarEntity c : calendars) {
                em.remove(c);
            }
            tx.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            tx.end();
        }
    }

    private WorkCalendarEntity createWorkCalendarEntity(Date day, String start, String end, Integer dayOfWeek) {
        WorkCalendarEntity calendarEntity = new WorkCalendarEntity();
        calendarEntity.setDay(day);
        calendarEntity.setStart(start);
        calendarEntity.setEnd(end);
        calendarEntity.setDayOfWeek(DayOfWeek.fromId(dayOfWeek));

        return calendarEntity;
    }

    private long getFirstIntervalDuration() {
        int i = 0;
        while (i++ < 365) {
            currentDay = Calendar.getInstance();
            currentDay.setTime(startTime);
            currentDay = DateUtils.truncate(currentDay, Calendar.DATE);
            List<CalendarItem> currentDayCalendarItems = exceptionDays.get(currentDay.getTime());
            if (currentDayCalendarItems == null)
                currentDayCalendarItems = defaultDays.get(currentDay.get(Calendar.DAY_OF_WEEK));

            ciIterator = currentDayCalendarItems.listIterator();
            while (ciIterator.hasNext()) {
                CalendarItem ci = ciIterator.next();

                if ((moveForward && ci.isDateBeforeInterval(startTime)) || (!moveForward && ci.isDateAfterInterval(startTime)))
                    return ci.getDuration();

                if (ci.isDateInInterval(startTime)) {
                    if (moveForward)
                        return ci.getDurationToEnd(startTime);
                    else
                        return ci.getDurationFromStart(startTime);
                }
            }

            if (moveForward)
                currentDay.add(Calendar.DAY_OF_YEAR, 1);
            else
                currentDay.add(Calendar.DAY_OF_YEAR, -1);
        }
        return 0;
    }

    private CalendarItem nextInterval() {
        if (ciIterator.hasNext())
            return ciIterator.next();

        if (moveForward)
            currentDay.add(Calendar.DAY_OF_YEAR, 1);
        else
            currentDay.add(Calendar.DAY_OF_YEAR, -1);

        List<CalendarItem> currentDayCalendarItems = exceptionDays.get(currentDay.getTime());
        if (currentDayCalendarItems == null)
            currentDayCalendarItems = defaultDays.get(currentDay.get(Calendar.DAY_OF_WEEK));

        ciIterator = currentDayCalendarItems.listIterator();
        return ciIterator.next();
    }

    private void reverseCaches() {
        for (int i = 1; i <= 7; i++) {
            List<CalendarItem> ciList = defaultDays.get(i);
            Collections.reverse(ciList);
        }
        for (Date date : exceptionDays.keySet()) {
            List<CalendarItem> ciList = exceptionDays.get(date);
            Collections.reverse(ciList);
        }
    }

    public Date addInterval(Date date, int qty, TimeUnit unit) {
        this.startTime = date;
        Calendar startTimeCalendar = Calendar.getInstance();
        startTimeCalendar.setTime(startTime);

        loadCaches();
        boolean prevMoveForward = moveForward;
        moveForward = (qty >= 0);
        if (moveForward != prevMoveForward)
            reverseCaches();
        long remainingTime;
        //Work day is not astronomic day
        if (unit.equals(TimeUnit.DAY))
            remainingTime = Math.abs(qty) * getWorkDayLengthInMillis();
        else
            remainingTime = Math.abs(qty) * unit.getMillis();

        long currentIntervalDuration = getFirstIntervalDuration();
        while (remainingTime > 0) {
            if (remainingTime <= currentIntervalDuration) {
                CalendarItem currentItem = ciIterator.previous();

                int finishDateBasicH;
                int finishDateBasicM;

                if (DateUtils.isSameDay(startTimeCalendar, currentDay) && currentItem.isDateInInterval(startTime)) {
                    finishDateBasicH = startTimeCalendar.get(Calendar.HOUR_OF_DAY);
                    finishDateBasicM = startTimeCalendar.get(Calendar.MINUTE);
                } else {
                    if (moveForward) {
                        finishDateBasicH = currentItem.getStartH();
                        finishDateBasicM = currentItem.getStartM();
                    } else {
                        finishDateBasicH = currentItem.getEndH();
                        finishDateBasicM = currentItem.getEndM();
                    }
                }

                currentDay.set(Calendar.HOUR_OF_DAY, finishDateBasicH);
                currentDay.set(Calendar.MINUTE, finishDateBasicM);

                if (moveForward) {
                    currentDay.add(Calendar.MILLISECOND, (int) remainingTime);
                } else {
                    currentDay.add(Calendar.MILLISECOND, -(int) remainingTime);
                }
                return currentDay.getTime();
            } else {
                remainingTime -= currentIntervalDuration;
                currentIntervalDuration = nextInterval().getDuration();
            }
        }
        return null;
    }

//    public Long getWorkPeriodDuration(Date startTime, Date endTime) {
//        long workIntervalDuration = 0;
//        loadCaches();
//        boolean prevMoveForward = moveForward;
//        moveForward = true;
//        if (moveForward != prevMoveForward)
//            reverseCaches();
//        this.startTime = startTime;
//
//        workIntervalDuration += getFirstIntervalDuration();
//        CalendarItem currentItem = nextInterval();
//        while (currentItem.isDateAfterInterval(endTime)) {
//           workIntervalDuration += currentItem.getDuration();
//            currentItem = nextInterval();
//        }
//
//        if (currentItem.isDateInInterval(endTime)) {
//            workIntervalDuration = currentItem.getDurationFromStart(endTime);
//        }
//
//        return workIntervalDuration;
//    }

    private boolean isDateWorkDay(Calendar day) {
        List<CalendarItem> currentDayCalendarItems = exceptionDays.get(day.getTime());
        if (currentDayCalendarItems == null)
            currentDayCalendarItems = defaultDays.get(day.get(Calendar.DAY_OF_WEEK));
        for (CalendarItem ci : currentDayCalendarItems) {
            if (ci.getDuration() > 0) return true;
        }
        return false;
    }

    public Long getWorkPeriodDurationInDays(Date startTime, Date endTime) {
        if ((startTime == null) || (endTime == null)) return 0L;
        if (startTime.compareTo(endTime) >= 0) return 0L;
        long workPeriodDuration = 0;
        loadCaches();
        boolean prevMoveForward = moveForward;
        moveForward = true;
        if (moveForward != prevMoveForward)
            reverseCaches();

        currentDay = Calendar.getInstance();
        currentDay.setTime(startTime);
        currentDay = DateUtils.truncate(currentDay, Calendar.DATE);

        while (!DateUtils.isSameDay(currentDay.getTime(), endTime)) {
            if (isDateWorkDay(currentDay))
                workPeriodDuration++;
            currentDay.add(Calendar.DAY_OF_YEAR, 1);
        }

        return workPeriodDuration;
    }    
}
