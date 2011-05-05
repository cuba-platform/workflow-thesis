package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.entity.AssignmentAttachment;

/**
 * <p>$Id$</p>
 *
 * @author chernov
 */
public class AssignmentAttachmentEntityListener implements BeforeInsertEntityListener<AssignmentAttachment>, BeforeDeleteEntityListener<AssignmentAttachment> {
    public void onBeforeInsert(AssignmentAttachment entity) {
        entity.getAssignment().getCard().setHasAttachments(true);
    }

    public void onBeforeDelete(AssignmentAttachment entity) {
        if (entity.getAssignment().getCard().getAttachments().size() == 1) entity.getAssignment().getCard().setHasAttachments(false);
    }
}