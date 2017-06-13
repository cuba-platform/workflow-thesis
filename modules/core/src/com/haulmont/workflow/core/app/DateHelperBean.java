/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.timer.OverdueAssignmentTimersFactory;
import org.jbpm.api.activity.ActivityExecution;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author d.evdokimov
 * @version $Id$
 */
@ManagedBean(DateHelperBean.NAME)
public class DateHelperBean {

    @Inject
    protected TimeSource timeSource;

    public static final String NAME = "wf_DateHelperBean";

    public void createOverdueTimers(ActivityExecution execution, Assignment assignment, CardRole cardRole) {
        WorkCalendarAPI workCalendar = AppBeans.get(WorkCalendarAPI.NAME);
        Date dueDate = workCalendar.addInterval(timeSource.currentTimestamp(), cardRole.getDuration(), cardRole.getTimeUnit());
        OverdueAssignmentTimersFactory overdueAssignmentTimersFactory = new OverdueAssignmentTimersFactory(dueDate);
        overdueAssignmentTimersFactory.setDueDate(dueDate);
        assignment.setDueDate(dueDate);

        EntityLoadInfo crLoadInfo = EntityLoadInfo.create(cardRole);
        Map<String, String> params = new HashMap<>();
        params.put("cardRole", crLoadInfo.toString());
        overdueAssignmentTimersFactory.createTimers(execution, assignment, params);
    }
}