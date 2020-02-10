/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardInfo;
import org.jbpm.api.activity.ActivityExecution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainAssignmentTimersFactory implements AssignmentTimersFactory {

    public void createTimers(ActivityExecution execution, Assignment assignment) {
        Map<String, String> params = new HashMap<>();
        EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());
        params.put("user", userLoadInfo.toString());
        WfHelper.getTimerManager().addTimer(
                assignment.getCard(),
                execution,
                assignment.getDueDate(),
                getClass(),
                MainAssignmentTimerAction.class,
                params);
    }

    public void removeTimers(ActivityExecution execution) {
        WfHelper.getTimerManager().removeTimers(execution);
        removeTimers(execution, null);
    }

    public void removeTimers(ActivityExecution execution, Assignment assignment) {
        if (assignment == null)
            WfHelper.getTimerManager().removeTimers(execution);
        else
            WfHelper.getTimerManager().removeTimers(execution, assignment);

        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        List<CardInfo> cardInfos = em.createQuery("select ci from wf$CardInfo ci " +
                "where ci.jbpmExecutionId = ?1 and ci.activity = ?2 and ci.user.id = ?3", CardInfo.class)
                .setParameter(1, execution.getId())
                .setParameter(2, execution.getActivityName())
                .setParameter(3, AppBeans.get(UserSessionSource.class).currentOrSubstitutedUserId())
                .getResultList();

        for (CardInfo cardInfo : cardInfos)
            em.remove(cardInfo);
    }
}
