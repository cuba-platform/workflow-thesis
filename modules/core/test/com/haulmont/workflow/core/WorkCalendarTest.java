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

        Long millis = workCalendar.getAbsoluteMillis(cal.getTime(), 10, TimeUnit.MINUTE);
        assertEquals(600000L, millis.longValue());
    }

    private void createStandardWorkTime() {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();

            WorkCalendarEntity ent = new WorkCalendarEntity();
            ent.setStart("0900");
            ent.setEnd("1230");
            em.persist(ent);

            ent = new WorkCalendarEntity();
            ent.setStart("1330");
            ent.setEnd("1800");
            em.persist(ent);
            
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
