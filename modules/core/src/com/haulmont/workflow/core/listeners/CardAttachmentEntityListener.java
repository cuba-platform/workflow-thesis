/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * Author: chernov
 * Created: 23.03.11 10:56
 *
 * $Id$
 */
package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.listener.*;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;

public class CardAttachmentEntityListener implements BeforeInsertEntityListener<CardAttachment>, BeforeDeleteEntityListener<CardAttachment> {
    public void onBeforeInsert(CardAttachment entity) {
        entity.getCard().setHasAttachments(true);
    }

    public void onBeforeDelete(CardAttachment entity) {
        Card card = entity.getCard();
        card.getAttachments().remove(entity);
        if (card.getAttachments().isEmpty()) {
            boolean hasAttachments = false;
            for (Assignment assignment : card.getAssignments()) {
                if (assignment.getAttachments().size() > 0) {
                    hasAttachments = true;
                    break;
                }
            }
            if (!hasAttachments) card.setHasAttachments(false);
        }
    }
}