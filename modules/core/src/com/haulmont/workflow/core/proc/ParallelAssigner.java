/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 16.11.2009 19:09:50
 *
 * $Id$
 */
package com.haulmont.workflow.core.proc;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import org.jbpm.api.activity.ActivityExecution;

public class ParallelAssigner extends Assigner {

    @Override
    protected void createAssignment(ActivityExecution execution, User user) {
        EntityManager em = PersistenceProvider.getEntityManager();

        Assignment assignment = new Assignment();
        assignment.setName(execution.getActivityName());
        assignment.setJbpmProcessId(execution.getProcessInstance().getId());
        assignment.setUser(user);

        em.persist(assignment);
    }
}
