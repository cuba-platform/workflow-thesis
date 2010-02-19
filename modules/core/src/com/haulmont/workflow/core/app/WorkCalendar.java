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
import com.haulmont.cuba.core.entity.AppFolder;
import com.haulmont.cuba.core.entity.Folder;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import com.haulmont.workflow.core.global.TimeUnit;
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

        private CalendarItem(Date day, String start, String end) {
            this.day = day;
            if (start != null) {
                this.startH = Integer.valueOf(start.substring(0, 2));
                this.startM = Integer.valueOf(start.substring(2));
            } else {
                this.startH = 0;
                this.startM = 0;
            }

            if (end != null) {
                this.endH = Integer.valueOf(end.substring(0, 2));
                this.endM = Integer.valueOf(end.substring(2));
            } else {
                this.endH = 0;
                this.endM = 0;
            }
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

        public Long getIntervalInMillis() {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, startH);
            startTime.set(Calendar.MINUTE, startM);
            endTime.set(Calendar.HOUR_OF_DAY, endH);
            endTime.set(Calendar.MINUTE, endM);
            return endTime.getTimeInMillis() - startTime.getTimeInMillis();
        }

        public Long getIntervalInMillis(Date fromDate) {
            Calendar startDay = Calendar.getInstance();
            startDay.setTime(fromDate);
            Calendar endDay = Calendar.getInstance();
            endDay.set(Calendar.HOUR_OF_DAY, endH);
            endDay.set(Calendar.MINUTE, endM);

            //we're interested only in hours and minutes
            startDay.clear(Calendar.YEAR);
            startDay.clear(Calendar.MONTH);
            startDay.clear(Calendar.DAY_OF_MONTH);
            endDay.clear(Calendar.YEAR);
            endDay.clear(Calendar.MONTH);
            endDay.clear(Calendar.DAY_OF_MONTH);

            return endDay.getTimeInMillis() - startDay.getTimeInMillis();
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


    }

    private volatile Map<Date, List<CalendarItem>> cache;

    private void loadCache() {
        if (cache == null) {
            synchronized (this) {
                if (cache == null) {
                    cache = new HashMap<Date, List<CalendarItem>>();
                    Transaction tx = Locator.createTransaction();
                    try {
                        EntityManager em = PersistenceProvider.getEntityManager();
                        Query q = em.createQuery("select c.day, c.start, c.end from wf$Calendar c where c.day >= CURRENT_TIMESTAMP " +
                                "or c.day is null order by c.day, c.start");
                        List<Object[]> list = q.getResultList();
                        for (Object[] row : list) {
                            Date date = (Date) row[0];
                            CalendarItem ci = new CalendarItem(date, (String) row[1], (String) row[2]);
                            List<CalendarItem> mapValue = cache.get(date);
                            if (mapValue == null)
                                mapValue = new LinkedList<CalendarItem>();
                            mapValue.add(ci);
                            cache.put(date, mapValue);
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
        Date endDate = addInterval(date, qty, unit);
        return endDate.getTime() - date.getTime();
    }

    public Date addInterval(Date date, int qty, TimeUnit unit) {
        loadCache();
        Long timeRemain;
        //Work day is not astronomic day
        if (unit.equals(TimeUnit.DAY))
            timeRemain = qty * getWorkDayLengthInMillis();
        else
            timeRemain = qty * unit.getMillis();

        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(date);
        currentDay = DateUtils.truncate(currentDay, Calendar.DAY_OF_MONTH);

        boolean isFirstDay = true;

        //walk through days beginning from current day and check whether our work time is expired
        while (timeRemain > 0) {
            List<CalendarItem> currentDayItems = cache.get(currentDay.getTime());
            if (currentDayItems == null)
                currentDayItems = cache.get(null);
            if (currentDayItems != null) {
                for (CalendarItem ci : currentDayItems) {
                    Long ciInterval;
                    //execution can start not in date when task is stated
                    //in such a case we consider first day of execution as usual day, NOT first
                    if (isFirstDay && !DateUtils.isSameDay(currentDay.getTime(), date)) {
                        isFirstDay = false;
                    }
                    if (isFirstDay) {
                        if (!ci.isDateInInterval(date)) continue;
                        ciInterval = ci.getIntervalInMillis(date);
                        isFirstDay = false;
                    } else
                        ciInterval = ci.getIntervalInMillis();
                    if (timeRemain > ciInterval)  {
                        timeRemain -= ciInterval;
                    } else {
                        Calendar finishDate = Calendar.getInstance();
                        finishDate.set(Calendar.YEAR, currentDay.get(Calendar.YEAR));
                        finishDate.set(Calendar.MONTH, currentDay.get(Calendar.MONTH));
                        finishDate.set(Calendar.DAY_OF_MONTH, currentDay.get(Calendar.DAY_OF_MONTH));
                        //if date is in first interval, we'll calculate finishDate from currentDate, not interval start
                        if (ci.isDateInInterval(date) && DateUtils.isSameDay(currentDay.getTime(), date)) {
                            currentDay.setTime(date);
                            finishDate.set(Calendar.HOUR_OF_DAY, currentDay.get(Calendar.HOUR_OF_DAY));
                            finishDate.set(Calendar.MINUTE, currentDay.get(Calendar.MINUTE));    
                        } else {
                            finishDate.set(Calendar.HOUR_OF_DAY, ci.getStartH());
                            finishDate.set(Calendar.MINUTE, ci.getStartM());    
                        }
                        finishDate.add(Calendar.MILLISECOND, timeRemain.intValue());

                        return finishDate.getTime();
                    }
                }
            } else {
                String msg = "Business calendar isn't defined correctly";
                throw new RuntimeException(msg);
            }
            currentDay.add(Calendar.DAY_OF_YEAR, 1);
        }
        return null;
    }

    public Long getWorkDayLengthInMillis() {
        loadCache();
        Long workDayLength = new Long(0);
        List<CalendarItem> defaultItems = cache.get(null);
        if (defaultItems != null) {
            for (CalendarItem calendarItem : defaultItems) {
                if (calendarItem.getDay() == null) {
                    workDayLength += calendarItem.getIntervalInMillis();
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

            String calendarResourceName = AppContext.getProperty("workflow.WorkCalendar.path");
            if (calendarResourceName == null) return "workflow.WorkCalendar.path property isn't set";
            ResourceRepositoryService rr = Locator.lookup(ResourceRepositoryService.NAME);
            if (rr.resourceExists(calendarResourceName)) {
                String xml = rr.getResAsString(calendarResourceName);
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();

                List<Date> filledDays = new ArrayList<Date>();

                DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
                for (Element dayElement : Dom4j.elements(root)) {
                    if ("workDay".equals(dayElement.getName())) {
                        em.persist(createWorkCalendarEntity(null, dayElement.attributeValue("start"), dayElement.attributeValue("end")));
                    } else if ("dayOff".equals(dayElement.getName())) {
                        Date date = df.parse(dayElement.attributeValue("date"));
                        em.persist(createWorkCalendarEntity(date, null, null));
                        filledDays.add(date);
                    } else if ("exceptionDay".equals(dayElement.getName())) {
                        Date date = df.parse(dayElement.attributeValue("date"));
                        em.persist(createWorkCalendarEntity(date, dayElement.attributeValue("start"), dayElement.attributeValue("end")));
                        filledDays.add(date);
                    }
                }

                //fill weekends except days already added to filledDays
                Calendar now = Calendar.getInstance();
                int currentYear = now.get(Calendar.YEAR);
                Calendar currentDay = GregorianCalendar.getInstance();
                currentDay.set(Calendar.MONTH, Calendar.JANUARY);
                currentDay.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                if (currentDay.get(Calendar.DAY_OF_MONTH) == 7)
                    currentDay.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                currentDay.set(Calendar.WEEK_OF_MONTH, 1);
                currentDay = DateUtils.truncate(currentDay, Calendar.DAY_OF_MONTH);
                while (currentDay.get(Calendar.YEAR) == currentYear) {
                    if (!filledDays.contains(currentDay.getTime()))
                        em.persist(createWorkCalendarEntity(currentDay.getTime(), null, null));
                    if (currentDay.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
                        currentDay.add(Calendar.DAY_OF_YEAR, 1);
                    else currentDay.add(Calendar.DAY_OF_YEAR, 6); 
                }
            }


/*
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
*/

            tx.commit();
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
            logout();
        }
        return "Work calendar records created successfuly";
    }

    private void deleteWorkCalendar() throws Exception{
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

    private WorkCalendarEntity createWorkCalendarEntity(Date day, String start, String end) {
        WorkCalendarEntity calendarEntity = new WorkCalendarEntity();
        calendarEntity.setDay(day);
        calendarEntity.setStart(start);
        calendarEntity.setEnd(end);

        return calendarEntity;
    }

}
