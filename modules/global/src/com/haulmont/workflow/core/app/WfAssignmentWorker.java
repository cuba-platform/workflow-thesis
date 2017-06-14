/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.Proc;

public interface WfAssignmentWorker {
    String NAME = "workflow_WfAssignmentWorker";

    Assignment createAssignment(String name, CardRole cardRole,
                                String description, String jbpmProcessId,
                                User user, Card card, Proc proc,
                                Integer iteration, Assignment familyAssignment,
                                Assignment master);
}
