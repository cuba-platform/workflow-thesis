/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.Proc;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

@ManagedBean(WfAssignmentWorker.NAME)
public class WfAssignmentWorkerBean implements WfAssignmentWorker {

    @Inject
    protected Metadata metadata;
    @Inject
    protected Persistence persistence;

    @Override
    public Assignment createAssignment(String name, CardRole cardRole, String description,
                                       String jbpmProcessId, User user, Card card, Proc proc,
                                       Integer iteration, Assignment familyAssignment, Assignment master) {
        Assignment assignment = metadata.create(Assignment.class);
        assignment.setName(name);
        if (cardRole != null) assignment.setCardRole(cardRole);
        assignment.setDescription(description);
        assignment.setJbpmProcessId(jbpmProcessId);
        assignment.setUser(user);
        assignment.setCard(card);
        assignment.setProc(proc);
        assignment.setIteration(iteration);
        assignment.setFamilyAssignment(familyAssignment);
        assignment.setMasterAssignment(master);
        return assignment;
    }
}
