/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class OverdueAssignmentTimersFactory implements AssignmentTimersFactory {

    private Date dueDate;

    public OverdueAssignmentTimersFactory() {
    }

    public OverdueAssignmentTimersFactory(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public void createTimers(ActivityExecution execution, Assignment assignment) {
        HashMap<String, String> params = new HashMap<String, String>();
        EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());
        params.put("user", userLoadInfo.toString());

        EntityLoadInfo assignmentLoadInfo = EntityLoadInfo.create(assignment);
        params.put("assignment", assignmentLoadInfo.toString());

        WfHelper.getTimerManager().addTimer(
                assignment.getCard(),
                execution,
                dueDate,
                OverdueAssignmentTimerAction.class,
                params
        );
    }

    public void createTimers(ActivityExecution execution, Assignment assignment, Map<String, String> params) {
        EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());
        params.put("user", userLoadInfo.toString());

        EntityLoadInfo assignmentLoadInfo = EntityLoadInfo.create(assignment);
        params.put("assignment", assignmentLoadInfo.toString());

        WfHelper.getTimerManager().addTimer(
                assignment.getCard(),
                execution,
                dueDate,
                OverdueAssignmentTimerAction.class,
                params
        );
    }

    @Override
    public void removeTimers(ActivityExecution execution) {
        removeTimers(execution, null);
    }

    @Override
    public void removeTimers(ActivityExecution execution, Assignment assignment) {
        String queryStr = "delete from wf$CardInfo ci where ci.jbpmExecutionId = ?1 and ci.activity = ?2";
        if (assignment == null) {
            WfHelper.getTimerManager().removeTimers(execution);
        } else {
            WfHelper.getTimerManager().removeTimers(execution, assignment);
            queryStr += " and ci.user.id = ?3";
        }

        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Query query = em.createQuery(queryStr);
        query.setParameter(1, execution.getId());
        query.setParameter(2, execution.getActivityName());

        if (assignment != null) {
            query.setParameter(3, assignment.getUser());
        }
        query.executeUpdate();
    }
}
