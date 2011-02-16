/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 08.12.2010 10:16:41
 *
 * $Id$
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.SecurityProvider;
import com.haulmont.cuba.core.global.EntityLoadInfo;
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

        EntityManager em = PersistenceProvider.getEntityManager();
        Query query = em.createQuery("delete from wf$CardInfo ci where ci.jbpmExecutionId = ?1 and ci.activity = ?2 " +
                "and ci.user.id = ?3");
        query.setParameter(1, execution.getId());
        query.setParameter(2, execution.getActivityName());
        query.setParameter(3, SecurityProvider.currentOrSubstitutedUserId());
        query.executeUpdate();
    }
}