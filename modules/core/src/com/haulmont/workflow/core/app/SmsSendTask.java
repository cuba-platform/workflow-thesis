/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Inject;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SmsSendTask implements Runnable {

    private SendingSms sendingSms;
    private Log log = LogFactory.getLog(SmsSendTask.class);

    @Inject
    private Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected WorkCalendarAPI workCalendar;

    @Inject
    protected SmsSenderAPI smsSender;

    public SmsSendTask(SendingSms message) {
        sendingSms = message;
    }

    @Override
    public void run() {
        Date currentTime = timeSource.currentTimestamp();
        if (selectedTimeIsWorkTime(currentTime)) {
            try {
                smsSender.scheduledSendSms(sendingSms);
            } catch (Exception e) {
                log.error("Exception while sending sms " + sendingSms);
            }
        } else {
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                Date nextStartDate = sendingSms.getStartSendingDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(nextStartDate);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                String queryForDayOfWeek = "select c from wf$Calendar c where c.dayOfWeek=:dayOfWeek";
                String queryForDay = "select c from wf$Calendar c where c.day=:day";
                Query queryDayOfWeek = em.createQuery(queryForDayOfWeek).setParameter("dayOfWeek", dayOfWeek);
                Query queryDay = em.createQuery(queryForDay).setParameter("day", nextStartDate);
                List<WorkCalendarEntity> calendarEntitiesDayOfWeek = queryDayOfWeek.getResultList();
                List<WorkCalendarEntity> calendarEntitiesDay = queryDay.getResultList();
                List<WorkCalendarEntity> calendarEntities = calendarEntitiesDay.isEmpty() ? calendarEntitiesDayOfWeek : calendarEntitiesDay;
                if (selectedDateIsWorkDay(nextStartDate)) {
                    Date endTime = null;
                    for (WorkCalendarEntity calendarEntity : calendarEntities) {
                        if (endTime == null) endTime = calendarEntity.getEnd();
                        else {
                            endTime = (calendarEntity.getEnd() != null && calendarEntity.getEnd().compareTo(endTime) == 1) ? calendarEntity.getEnd() : endTime;
                        }
                    }
                    nextStartDate = DateUtils.truncate(nextStartDate, Calendar.DATE);
                    if (endTime != null) {
                        Calendar calendarEndTime = Calendar.getInstance();
                        calendarEndTime.setTime(endTime);
                        Date currentDate = DateUtils.truncate(currentTime, Calendar.DATE);
                        if (currentTime.getTime() - currentDate.getTime() >=
                                ((calendarEndTime.get(Calendar.HOUR_OF_DAY) * 60 + calendarEndTime.get(Calendar.MINUTE)) * 60 * 1000)) {
                            nextStartDate = DateUtils.addDays(nextStartDate, 1);
                        }
                    }
                } else {
                    nextStartDate = DateUtils.addDays(nextStartDate, 1);
                    nextStartDate = DateUtils.truncate(nextStartDate, Calendar.DATE);
                }
                while (!selectedDateIsWorkDay(nextStartDate)) {
                    nextStartDate = DateUtils.addDays(nextStartDate, 1);
                }
                calendar.setTime(nextStartDate);
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                queryDayOfWeek = em.createQuery(queryForDayOfWeek).setParameter("dayOfWeek", dayOfWeek);
                queryDay = em.createQuery(queryForDay).setParameter("day", nextStartDate);
                calendarEntitiesDayOfWeek = queryDayOfWeek.getResultList();
                calendarEntitiesDay = queryDay.getResultList();
                Date startTime = null;
                calendarEntities = calendarEntitiesDay.isEmpty() ? calendarEntitiesDayOfWeek : calendarEntitiesDay;
                for (WorkCalendarEntity calendarEntity : calendarEntities) {
                    if (startTime == null) startTime = calendarEntity.getStart();
                    else {
                        startTime = (calendarEntity.getStart() != null && calendarEntity.getStart().compareTo(startTime) == -1) ? calendarEntity.getStart() : startTime;
                    }
                }
                calendar.setTime(startTime);
                if (!nextStartDate.equals(DateUtils.truncate(sendingSms.getStartSendingDate(), Calendar.DATE))
                        || (sendingSms.getStartSendingDate().getTime() - nextStartDate.getTime()) <
                        (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)) * 60 * 1000) {
                    nextStartDate = DateUtils.setHours(nextStartDate, calendar.get(Calendar.HOUR_OF_DAY));
                    nextStartDate = DateUtils.setMinutes(nextStartDate, calendar.get(Calendar.MINUTE));
                    sendingSms.setStartSendingDate(nextStartDate);
                    em.merge(sendingSms);
                }
                tx.commit();
            } catch (Exception e) {
                log.error("Error in schedule sendingSms waiting", e);
            } finally {
                tx.end();
            }
        }
    }

    protected boolean selectedTimeIsWorkTime(Date selectedDate) {
        return workCalendar.isTimeWorkTime(selectedDate);
    }

    protected boolean selectedDateIsWorkDay(Date selectedDate) {
        return workCalendar.isDateWorkDay(selectedDate);
    }
}
