/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.global;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AssignmentInfo implements Serializable {

    private static final long serialVersionUID = 1555534603461492878L;

    private UUID assignmentId;
    private String name;
    private String description;
    private List<String> actions;
    private Card card;

    public AssignmentInfo(Assignment assignment) {
        assignmentId = assignment.getId();
        name = assignment.getName();
        description = assignment.getDescription();
        actions = new ArrayList();
        //todo: store card in assignment info (its a hack)
        card = assignment.getCard();
        if (card != null)
            card.getProc();
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

    public Card getCard() {
        return card;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AssignmentInfo info = (AssignmentInfo) o;

        if (assignmentId != null ? !assignmentId.equals(info.assignmentId) : info.assignmentId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return assignmentId != null ? assignmentId.hashCode() : 0;
    }
}
