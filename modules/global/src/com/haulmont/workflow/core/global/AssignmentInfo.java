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

}
