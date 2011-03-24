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
import com.haulmont.workflow.core.entity.CardAttachment;

public class CardAttachmentEntityListener implements BeforeInsertEntityListener<CardAttachment>, BeforeDeleteEntityListener<CardAttachment> {
    public void onBeforeInsert(CardAttachment entity) {
        entity.getCard().setHasAttachments(true);
    }

    public void onBeforeDelete(CardAttachment entity) {
        if (entity.getCard().getAttachments().size() == 1) entity.getCard().setHasAttachments(false);
    }
}
