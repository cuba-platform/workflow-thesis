/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 16.11.2009 12:32:08
 *
 * $Id$
 */
package com.haulmont.workflow.core.proc;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import org.apache.commons.lang.StringUtils;
import org.jbpm.api.activity.ActivityExecution;
import org.jbpm.api.activity.ExternalActivityBehaviour;

import java.util.List;
import java.util.Map;

public class Assigner implements ExternalActivityBehaviour {

    private String assignee;

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public void execute(ActivityExecution execution) throws Exception {
        Preconditions.checkState(!StringUtils.isBlank(assignee), "Assignee is blank");

        Transaction tx = Locator.getTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query q = em.createQuery("select u from sec$User u where u.loginLowerCase = ?1");
            q.setParameter(1, assignee.toLowerCase());
            List<User> list = q.getResultList();
            if (list.isEmpty())
                throw new RuntimeException("User not found: " + assignee);
            User user = list.get(0);

            createAssignment(execution, user);

            execution.waitForSignal();

            tx.commit();
        } finally {
            tx.end();
        }
    }

    protected void createAssignment(ActivityExecution execution, User user) {
        EntityManager em = PersistenceProvider.getEntityManager();

        Assignment assignment = new Assignment();
        assignment.setName(execution.getActivityName());
        assignment.setJbpmProcessId(execution.getProcessInstance().getId());
        assignment.setUser(user);

        em.persist(assignment);
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        if (parameters != null) {
            execution.setVariables(parameters);
        }
        execution.take(signalName);
    }
}
