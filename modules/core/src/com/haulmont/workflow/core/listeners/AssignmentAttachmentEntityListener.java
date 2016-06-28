/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.AssignmentAttachment;
import com.haulmont.workflow.core.entity.Card;

public class AssignmentAttachmentEntityListener implements BeforeInsertEntityListener<AssignmentAttachment>, BeforeDeleteEntityListener<AssignmentAttachment> {
    @Override
    public void onBeforeInsert(AssignmentAttachment entity) {
        entity.getAssignment().getCard().setHasAttachments(true);
    }

    @Override
    public void onBeforeDelete(AssignmentAttachment entity) {
        Card card = entity.getAssignment().getCard();
        Assignment assignment = entity.getAssignment();
        assignment.getAttachments().remove(entity);
        if (card.getAttachments().isEmpty()) {
            boolean hasAttachments = false;
            for (Assignment a : card.getAssignments()) {
                if (a.getAttachments().size() > 0) {
                    hasAttachments = true;
                    break;
                }
            }
            if (!hasAttachments) card.setHasAttachments(false);
        }
    }
}