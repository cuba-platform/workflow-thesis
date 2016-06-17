/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
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
                d = new Date(TimeProvider.currentTimestamp().getTime() + WfHelper.getTimeMillis(dueDate));
            }
            EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());

            Map<String, String> params = new HashMap<String, String>();
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

        EntityManager em = PersistenceProvider.getEntityManager();
        Query query = em.createQuery("delete from wf$CardInfo ci where ci.jbpmExecutionId = ?1 and ci.activity = ?2 " +
                "and ci.user.id = ?3");
        query.setParameter(1, execution.getId());
        query.setParameter(2, execution.getActivityName());
        query.setParameter(3, UserSessionProvider.currentOrSubstitutedUserId());
        query.executeUpdate();
    }
}
