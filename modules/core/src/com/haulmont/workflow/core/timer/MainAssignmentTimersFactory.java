/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import org.jbpm.api.activity.ActivityExecution;

import java.util.HashMap;
import java.util.Map;

public class MainAssignmentTimersFactory implements AssignmentTimersFactory{
    
    public void createTimers(ActivityExecution execution, Assignment assignment) {
        Map<String, String> params = new HashMap<String, String>();
        EntityLoadInfo userLoadInfo = EntityLoadInfo.create(assignment.getUser());
        params.put("user", userLoadInfo.toString());
        WfHelper.getTimerManager().addTimer(assignment.getCard(), execution, assignment.getDueDate(),
                MainAssignmentTimerAction.class, params);
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
