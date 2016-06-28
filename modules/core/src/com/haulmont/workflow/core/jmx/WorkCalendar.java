/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.app.WorkCalendarAPI;
import com.haulmont.workflow.core.entity.DayOfWeek;
import com.haulmont.workflow.core.entity.WorkCalendarEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("workflow_WorkCalendarMBean")
public class WorkCalendar implements WorkCalendarMBean {

    @Inject
    protected WorkCalendarAPI workCalendar;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected Resources resources;

    @Override
    public int getCacheSize() {
        return workCalendar.getCacheSize();
    }

    @Override
    public void invalidateCache() {
        workCalendar.invalidateCache();
    }

    @Authenticated
    @Override
    public String fillWorkCalendar() {
        Transaction tx = persistence.createTransaction();
        try {
            deleteWorkCalendar();
            EntityManager em = persistence.getEntityManager();

            String calendarResourceName = AppContext.getProperty("workflow.workCalendar.path");
            if (calendarResourceName == null) return "workflow.workCalendar.path property isn't set";

            String xml = resources.getResourceAsString(calendarResourceName);
            if (xml != null) {
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();

                List<Date> filledDays = new ArrayList<>();

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
        }
        return "Work calendar records created successfuly";
    }

    private void deleteWorkCalendar() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();

            em.setSoftDeletion(false);
            Query attrQuery = em.createQuery("select c from wf$Calendar c");
            List<WorkCalendarEntity> calendars = attrQuery.getResultList();
            for (WorkCalendarEntity c : calendars) {
                em.remove(c);
            }
            tx.commit();
        } finally {
            tx.end();
        }
    }

    private WorkCalendarEntity createWorkCalendarEntity(Date day, String start, String end, Integer dayOfWeek) {
        WorkCalendarEntity calendarEntity = metadata.create(WorkCalendarEntity.class);
        calendarEntity.setDay(day);
        SimpleDateFormat format = new SimpleDateFormat("hh:mm");
        try {
            if (!StringUtils.isEmpty(start))
                calendarEntity.setStart(format.parse(start));
            if (!StringUtils.isEmpty(end))
                calendarEntity.setEnd(format.parse(end));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        calendarEntity.setDayOfWeek(DayOfWeek.fromId(dayOfWeek));

        return calendarEntity;
    }
}