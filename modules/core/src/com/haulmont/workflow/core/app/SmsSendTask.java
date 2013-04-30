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
        if (selectedTimeIsWorkTime(timeSource.currentTimestamp())) {
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
                String queryString = "select c from wf$Calendar c where c.dayOfWeek=:dayOfWeek or c.day=:day";
                Query query = em.createQuery(queryString).setParameter("dayOfWeek", dayOfWeek).setParameter("day", nextStartDate);
                List<WorkCalendarEntity> calendarEntities = query.getResultList();
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
                        if (sendingSms.getStartSendingDate().getTime() - nextStartDate.getTime() >
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
                queryString = "select c from wf$Calendar c where c.dayOfWeek=:dayOfWeek or c.day=:day";
                query = em.createQuery(queryString).setParameter("dayOfWeek", dayOfWeek).setParameter("day", nextStartDate);

                Date startTime = null;
                calendarEntities = query.getResultList();
                for (WorkCalendarEntity calendarEntity : calendarEntities) {
                    if (startTime == null) startTime = calendarEntity.getStart();
                    else {
                        startTime = (calendarEntity.getStart() != null && calendarEntity.getStart().compareTo(startTime) == -1) ? calendarEntity.getStart() : startTime;
                    }
                }
                calendar.setTime(startTime);
                nextStartDate = DateUtils.setHours(nextStartDate, calendar.get(Calendar.HOUR_OF_DAY));
                nextStartDate = DateUtils.setMinutes(nextStartDate, calendar.get(Calendar.MINUTE));
                sendingSms.setStartSendingDate(nextStartDate);
                em.merge(sendingSms);
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
