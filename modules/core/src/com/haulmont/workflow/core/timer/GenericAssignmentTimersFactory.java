/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardInfo;
import org.apache.commons.lang.StringUtils;
import org.jbpm.api.activity.ActivityExecution;

import java.util.*;

public class GenericAssignmentTimersFactory implements AssignmentTimersFactory {

    protected String[] dueDatesArr;

    protected String[] transitionsArr;

    protected String[] scriptsArr;

    public void setDueDates(String dueDates) {
        this.dueDatesArr = dueDates.split("\\|");
    }

    public void setTransitions(String transitions) {
        this.transitionsArr = transitions.split("\\|");
    }

    public void setScripts(String scripts) {
        this.scriptsArr = scripts.split("\\|");
    }

    public void createTimers(ActivityExecution execution, Assignment assignment) {
        for (int i = 0; i < dueDatesArr.length; i++) {
            String dueDate = dueDatesArr[i];
            Date d;
            if ("process".equals(StringUtils.trimToNull(dueDate))) {
                d = (Date) execution.getVariable("dueDate");
                if (d == null)
                    continue;
            } else {
                d = new Date(AppBeans.get(TimeSource.class).currentTimestamp().getTime() + WfHelper.getTimeMillis(dueDate));
            }
            EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());

            Map<String, String> params = new HashMap<>();
            params.put("user", userLoadInfo.toString());

            if (transitionsArr.length > i && !StringUtils.isBlank(transitionsArr[i])) {
                params.put("transition", transitionsArr[i]);
                assignment.setDueDate(d);
            } else if (scriptsArr.length > i && !StringUtils.isBlank(scriptsArr[i])) {
                params.put("script", scriptsArr[i]);
                if ("process".equals(StringUtils.trimToNull(dueDate)))
                    assignment.setDueDate(d);
            }

            WfHelper.getTimerManager().addTimer(
                    assignment.getCard(),
                    execution,
                    d,
                    getClass(),
                    GenericAssignmentTimerAction.class,
                    params
            );
        }
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
