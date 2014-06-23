/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import com.haulmont.workflow.core.global.TimeUnit;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.*;

@ManagedBean(WorkCalendarAPI.NAME)
public class WorkCalendar implements WorkCalendarAPI {

    protected Log log = LogFactory.getLog(WorkCalendar.class);

    protected volatile Map<Date, List<CalendarItem>> exceptionDays;
    protected volatile Map<Integer, List<CalendarItem>> defaultDays;

    @Inject
    protected Persistence persistence;

    protected synchronized void loadCaches() {
        if (exceptionDays == null) {
            exceptionDays = new HashMap<>();
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                Query q = em.createQuery("select c from wf$Calendar c where c.day is not null " +
                        "order by c.day, c.start");
                List<WorkCalendarEntity> list = q.getResultList();
                for (WorkCalendarEntity c : list) {
                    CalendarItem ci = new CalendarItem(c);
                    List<CalendarItem> mapValue = exceptionDays.get(c.getDay());
                    if (mapValue == null)
                        mapValue = new LinkedList<>();
                    mapValue.add(ci);
                    exceptionDays.put(c.getDay(), mapValue);
                }

                tx.commit();
            } finally {
                tx.end();
            }
        }

        if (defaultDays == null) {
            defaultDays = new HashMap<>();
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                Query q = em.createQuery("select c from wf$Calendar c where c.dayOfWeek is not null " +
                        "order by c.dayOfWeek, c.start");
                List<WorkCalendarEntity> list = q.getResultList();
                for (WorkCalendarEntity c : list) {

                    CalendarItem ci = new CalendarItem(c);
                    List<CalendarItem> mapValue = defaultDays.get(c.getDayOfWeek().getId());
                    if (mapValue == null)
                        mapValue = new LinkedList<>();
                    mapValue.add(ci);
                    defaultDays.put(c.getDayOfWeek().getId(), mapValue);
                }
                tx.commit();
            } finally {
                tx.end();
            }
        }
    }

    @Override
    public int getCacheSize() {
        return exceptionDays == null ? 0 : exceptionDays.size();
    }

    @Override
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

    private FirstIntervalDurationResult getFirstIntervalDuration(Date startTime, boolean moveForward, Map<Date, List<CalendarItem>> _exceptionDays,
                                          Map<Integer, List<CalendarItem>> _defaultDays) {
        FirstIntervalDurationResult fidResult = new FirstIntervalDurationResult();
        int i = 0;
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(startTime);
        currentDay = DateUtils.truncate(currentDay, Calendar.DATE);
        fidResult.setCurrentDay(currentDay);

        Calendar startDay = Calendar.getInstance();
        startDay.setTime(startTime);
        while (i++ < 365) {
            List<CalendarItem> currentDayCalendarItems = _exceptionDays.get(currentDay.getTime());
            if (currentDayCalendarItems == null)
                currentDayCalendarItems = _defaultDays.get(currentDay.get(Calendar.DAY_OF_WEEK));

            ListIterator<CalendarItem> ciIterator = currentDayCalendarItems.listIterator();
            fidResult.setCiIterator(ciIterator);
            while (ciIterator.hasNext()) {
                CalendarItem ci = ciIterator.next();

                if ((moveForward && (ci.isDateBeforeInterval(startTime) || (currentDay.after(startDay))))
                        || (!moveForward && (ci.isDateAfterInterval(startTime)))) {
                    fidResult.setDuration(ci.getDuration());
                    return fidResult;
                }

                if (ci.isDateInInterval(startTime)) {
                    if (moveForward) {
                        fidResult.setDuration(ci.getDurationToEnd(startTime));
                        return fidResult;
                    } else {
                        fidResult.setDuration(ci.getDurationFromStart(startTime));
                        return fidResult;
                    }
                }
            }

            if (moveForward) {
                currentDay.add(Calendar.DAY_OF_YEAR, 1);
            } else {
                currentDay.add(Calendar.DAY_OF_YEAR, -1);
            }
        }

        fidResult.setDuration(0);
        return fidResult;
    }

    private ListIterator<CalendarItem> nextIntervalIterator(ListIterator<CalendarItem> ciIterator, Calendar currentDay,
                                            boolean moveForward, Map<Date, List<CalendarItem>> _exceptionDays,
                                            Map<Integer, List<CalendarItem>> _defaultDays) {
        if (ciIterator.hasNext()) {
            return ciIterator;
        }

        if (moveForward)
            currentDay.add(Calendar.DAY_OF_YEAR, 1);
        else
            currentDay.add(Calendar.DAY_OF_YEAR, -1);

        List<CalendarItem> currentDayCalendarItems = _exceptionDays.get(currentDay.getTime());
        if (currentDayCalendarItems == null)
            currentDayCalendarItems = _defaultDays.get(currentDay.get(Calendar.DAY_OF_WEEK));

        ciIterator = currentDayCalendarItems.listIterator();
        return ciIterator;
    }

    public Double getIntervalDuration(Date startTime, Date endTime, TimeUnit timeUnit) {
        if (startTime.after(endTime))
            throw new IllegalStateException("Start time cannot be after end time!");

        loadCaches();
        double duration = 0;
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(startTime);

        Calendar endDay = Calendar.getInstance();
        endDay.setTime(endTime);

        long timeUnitDuaration = (timeUnit == TimeUnit.DAY) ? getWorkDayLengthInMillis() : timeUnit.getMillis();

        boolean searchingFirstInterval = true;

        for (int i = 0; i < 365; i++) {
            List<CalendarItem> currentDayCalendarItems = exceptionDays.get(DateUtils.truncate(currentDay.getTime(), Calendar.DATE));
            if (currentDayCalendarItems == null)
                currentDayCalendarItems = defaultDays.get(currentDay.get(Calendar.DAY_OF_WEEK));

            int ciPos = 0;
            for (CalendarItem ci : currentDayCalendarItems) {
                ciPos++;

                if (DateUtils.isSameDay(currentDay, endDay)) {
                    if (ci.isDateInInterval(endTime)) {
                        if (searchingFirstInterval && DateUtils.isSameDay(currentDay.getTime(), startTime)) {
                            //if necessary interval is in first CI interval
                            if (ci.isDateInInterval(currentDay.getTime()))
                                duration = endDay.getTimeInMillis() - currentDay.getTimeInMillis();
                            else
                                duration = endDay.getTimeInMillis() - ci.getRealStartDay(currentDay.getTime()).getTimeInMillis();
                        } else {
                            duration += ci.getDurationFromStart(endTime);
                        }
                        return duration / timeUnitDuaration;
                    }
                    if (ci.isDateBeforeInterval(endTime)) {
                        return duration / timeUnitDuaration;
                    }
                    if (ciPos == currentDayCalendarItems.size()) {
                        if (ci.isDateInInterval(startTime)) {
                            duration = ci.getDurationToEnd(startTime);
                        } else if (!(searchingFirstInterval && ci.isDateAfterInterval(startTime))) {
                            duration += ci.getDuration();
                        }
                        return duration / timeUnitDuaration;
                    }
                }

                if (searchingFirstInterval) {
                    //truncate currentDay if we move to next day
                    if (!DateUtils.isSameDay(currentDay.getTime(), startTime)) {
                        currentDay = DateUtils.truncate(currentDay, Calendar.DATE);
                    }

                    if (ci.isDateInInterval(currentDay.getTime())) {
                        duration += ci.getDurationToEnd(currentDay.getTime());
                        searchingFirstInterval = false;
                    } else if (ci.isDateBeforeInterval(currentDay.getTime())) {
                        duration += ci.getDuration();
                        searchingFirstInterval = false;
                    }
                } else {
                    duration += ci.getDuration();
                }

            } //end loop for CalendarItems

            currentDay.add(Calendar.DAY_OF_YEAR, 1);

        } //end main loop

        return duration / timeUnitDuaration;
    }

    public Date addInterval(Date date, int qty, TimeUnit unit) {
        Date startTime = date;
        Calendar startTimeCalendar = Calendar.getInstance();
        startTimeCalendar.setTime(startTime);

        loadCaches();
        Map<Integer, List<CalendarItem>> _defaultDays = dayMapDeepCopy(defaultDays);
        Map<Date, List<CalendarItem>> _exceptionDays = dayMapDeepCopy(exceptionDays);

        boolean moveForward = (qty >= 0);
        if (!moveForward) {
            for (int i = 1; i <= 7; i++) {
                List<CalendarItem> ciList = _defaultDays.get(i);
                Collections.reverse(ciList);
            }
            for (Date _date : _exceptionDays.keySet()) {
                List<CalendarItem> ciList = _exceptionDays.get(_date);
                Collections.reverse(ciList);
            }
        }

        long remainingTime;
        //Work day is not astronomic day
        if (unit.equals(TimeUnit.DAY))
            remainingTime = Math.abs(qty) * getWorkDayLengthInMillis();
        else
            remainingTime = Math.abs(qty) * unit.getMillis();

        FirstIntervalDurationResult fidResult = getFirstIntervalDuration(startTime, moveForward, _exceptionDays, _defaultDays);
        long currentIntervalDuration = fidResult.getDuration();

        ListIterator<CalendarItem> ciIterator = fidResult.getCiIterator();
        Calendar currentDay = fidResult.getCurrentDay();

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
                ciIterator = nextIntervalIterator(ciIterator, currentDay, moveForward, _exceptionDays, _defaultDays);
                currentIntervalDuration = ciIterator.next().getDuration();
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

    public boolean isDateWorkDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return isDateWorkDay(calendar);
    }

    public boolean isDateWorkDay(Calendar day) {
        day = DateUtils.truncate(day, Calendar.DATE);
        loadCaches();
        List<CalendarItem> currentDayCalendarItems = exceptionDays.get(day.getTime());
        if (currentDayCalendarItems == null)
            currentDayCalendarItems = defaultDays.get(day.get(Calendar.DAY_OF_WEEK));
        if (currentDayCalendarItems == null)
            return false;

        for (CalendarItem ci : currentDayCalendarItems) {
            if (ci.getStartH() == 0 && ci.getEndH() == 0 && ci.getStartM() == 0 && ci.getEndM() == 0) {
                // If time is not specified, day is not a work day
                return false;
            }
            if (ci.getDuration() > 0) return true;
        }
        return false;
    }

    @Override
    public boolean isTimeWorkTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return isTimeWorkTime(calendar);
    }

    @Override
    public boolean isTimeWorkTime(Calendar day) {
        Calendar dayTime = day;
        day = DateUtils.truncate(day, Calendar.DATE);
        loadCaches();
        List<CalendarItem> currentDayCalendarItems = exceptionDays.get(day.getTime());
        if (currentDayCalendarItems == null)
            currentDayCalendarItems = defaultDays.get(day.get(Calendar.DAY_OF_WEEK));
        if (currentDayCalendarItems == null)
            return false;

        for (CalendarItem ci : currentDayCalendarItems) {
            if (ci.getDuration() > 0) {
                long currentTime = dayTime.getTimeInMillis() - day.getTimeInMillis();
                if (currentTime < (ci.getEndH() * 60 * 60 * 1000 + ci.getEndM() * 60 * 1000) && currentTime > (ci.getStartH() * 60 * 60 * 1000 + ci.getStartM() * 60 * 1000))
                    return true;
            }
        }
        return false;
    }

    public Long getWorkPeriodDurationInDays(Date startTime, Date endTime) {
        if ((startTime == null) || (endTime == null)) return 0L;
        if (startTime.compareTo(endTime) >= 0) return 0L;
        long workPeriodDuration = 0;
        loadCaches();
        Calendar currentDay = Calendar.getInstance();
        currentDay.setTime(startTime);
        currentDay = DateUtils.truncate(currentDay, Calendar.DATE);

        Calendar endDay = Calendar.getInstance();
        endDay.setTime(endTime);
        endDay = DateUtils.truncate(endDay, Calendar.DATE);

        while (currentDay.compareTo(endDay) <= 0) {
            if (isDateWorkDay(currentDay))
                workPeriodDuration++;
            currentDay.add(Calendar.DAY_OF_YEAR, 1);
        }

        return workPeriodDuration;
    }


    /**
     * Creates deep copy of defaultDays map and exceptionDays map
     * @param mapToClone
     * @param <T>
     * @return
     */
    private <T extends Map> T dayMapDeepCopy(T mapToClone) {
        T result = null;
        try {
            result = (T) mapToClone.getClass().newInstance();
        } catch (Exception e) {
            log.error(e);
        }
        for (Object key : mapToClone.keySet()) {
            Object value = mapToClone.get(key);
            LinkedList<CalendarItem> valueCopy = new LinkedList<CalendarItem>((Collection) value);
            result.put(key, valueCopy);
        }
        return result;
    }

    /**
     * Class is used to hold multiple return values from method {@link #getFirstIntervalDuration}
     */
    private class FirstIntervalDurationResult {
        private long duration;
        private ListIterator<CalendarItem> ciIterator;
        private Calendar currentDay;

        private FirstIntervalDurationResult() {
        }

        private long getDuration() {
            return duration;
        }

        private void setDuration(long duration) {
            this.duration = duration;
        }

        private ListIterator<CalendarItem> getCiIterator() {
            return ciIterator;
        }

        private void setCiIterator(ListIterator<CalendarItem> ciIterator) {
            this.ciIterator = ciIterator;
        }

        private Calendar getCurrentDay() {
            return currentDay;
        }

        private void setCurrentDay(Calendar currentDay) {
            this.currentDay = currentDay;
        }
    }

    public class CalendarItem {
        private final Date day;
        private final int startH;
        private final int startM;
        private final int endH;
        private final int endM;
        private final Integer dayOfWeek;

        public CalendarItem(WorkCalendarEntity entity) {
            this.day = entity.getDay();
            this.dayOfWeek = entity.getDayOfWeek() == null ? null : entity.getDayOfWeek().getId();

            if (entity.getStart() != null) {
                this.startH = (int) DateUtils.getFragmentInHours(entity.getStart(), Calendar.DAY_OF_YEAR);
                this.startM = (int) DateUtils.getFragmentInMinutes(entity.getStart(), Calendar.HOUR_OF_DAY);
            } else {
                this.startH = 0;
                this.startM = 0;
            }

            if (entity.getEnd() != null) {
                this.endH = (int) DateUtils.getFragmentInHours(entity.getEnd(), Calendar.DAY_OF_YEAR);
                this.endM = (int) DateUtils.getFragmentInMinutes(entity.getEnd(), Calendar.HOUR_OF_DAY);
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
                if (minutes <= endM) return true;
                else return false;
            }
            return false;
        }

        public Long getDuration() {
            Calendar startTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();

            startTime.set(Calendar.HOUR_OF_DAY, startH);
            startTime.set(Calendar.MINUTE, startM);
            startTime.set(Calendar.SECOND, 0);
            startTime.set(Calendar.MILLISECOND, 0);

            endTime.set(Calendar.HOUR_OF_DAY, endH);
            endTime.set(Calendar.MINUTE, endM);
            endTime.set(Calendar.SECOND, 0);
            endTime.set(Calendar.MILLISECOND, 0);

            return endTime.getTimeInMillis() - startTime.getTimeInMillis();
        }


        public boolean isDateBeforeInterval(Date date) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            int day = calendar.get(Calendar.DAY_OF_WEEK);
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
            int day = calendar.get(Calendar.DAY_OF_WEEK);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            if ((hour > endH) || ((hour == endH) && (minutes > endM))) {
                return true;
            }
            return false;
        }


        public Calendar getRealStartDay(Date base) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(base);
            cal.set(Calendar.HOUR_OF_DAY, startH);
            cal.set(Calendar.MINUTE, startM);

            return cal;
        }

        public Calendar getRealEndDay(Date base) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(base);
            cal.set(Calendar.HOUR_OF_DAY, endH);
            cal.set(Calendar.MINUTE, endM);

            return cal;
        }

    }

}
