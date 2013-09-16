/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.app.WfWorkerAPI;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;

public class CardAttachmentEntityListener implements BeforeInsertEntityListener<CardAttachment>, BeforeDeleteEntityListener<CardAttachment> {
    public void onBeforeInsert(CardAttachment entity) {
        entity.getCard().setHasAttachments(true);
        if (!PersistenceHelper.isNew(entity.getCard()))
            setHasAttachmentsInCard(entity.getCard(), true);
    }

    public void onBeforeDelete(CardAttachment entity) {
        Card card = entity.getCard();
        card.getAttachments().remove(entity);
        if (card.getAttachments().isEmpty()) {
            card.setHasAttachments(false);
            setHasAttachmentsInCard(card, false);
        }
    }

    private void setHasAttachmentsInCard(Card card, Boolean hasAttachments) {
        WfWorkerAPI wfWorkerAPI = AppBeans.get(WfWorkerAPI.NAME);
        wfWorkerAPI.setHasAttachmentsInCard(card, hasAttachments);
    }
}