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
import com.haulmont.workflow.core.entity.CardInfo;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class OverdueAssignmentTimersFactory implements AssignmentTimersFactory {
    public static final String NAME = "workflow_OverdueAssignmentTimersFactory";

    @Override
    public void createTimers(ActivityExecution execution, Assignment assignment) {
        EntityLoadInfo crLoadInfo = EntityLoadInfo.create(assignment.getCardRole());

        Map<String, String> params = new HashMap<>();
        params.put("cardRole", crLoadInfo.toString());

        EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());
        params.put("user", userLoadInfo.toString());

        EntityLoadInfo assignmentLoadInfo = EntityLoadInfo.create(assignment);
        params.put("assignment", assignmentLoadInfo.toString());

        Date dueDate = AppBeans.get(OverdueAssignmentDueDateHelperBean.class).getDueDate(assignment);

        WfHelper.getTimerManager().addTimer(
                assignment.getCard(),
                execution,
                dueDate,
                getClass(),
                OverdueAssignmentTimerAction.class,
                params
        );

        assignment.setDueDate(dueDate);
    }

    @Override
    public void removeTimers(ActivityExecution execution) {
        removeTimers(execution, null);
    }

    @Override
    public void removeTimers(ActivityExecution execution, Assignment assignment) {
        String queryStr = "select ci from wf$CardInfo ci where ci.jbpmExecutionId = ?1 and ci.activity = ?2";
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
        List<CardInfo> cardInfos = query.getResultList();
        for (CardInfo cardInfo : cardInfos) {
            em.remove(cardInfo);
        }
    }
}
