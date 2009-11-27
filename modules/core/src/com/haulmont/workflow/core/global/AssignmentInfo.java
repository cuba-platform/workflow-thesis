/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.11.2009 16:05:59
 *
 * $Id$
 */
package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.entity.Assignment;

import java.io.Serializable;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

public class AssignmentInfo implements Serializable {

    private static final long serialVersionUID = 1555534603461492878L;

    private UUID assignmentId;
    private String name;
    private String description;
    private List<String> actions;

    public AssignmentInfo(Assignment assignment) {
        assignmentId = assignment.getId();
        name = assignment.getName();
        description = assignment.getDescription();
        actions = new ArrayList();
    }

    public List<String> getActions() {
        return actions;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }
}
