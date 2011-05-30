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

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.listener.*;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import org.apache.commons.lang.BooleanUtils;

public class CardAttachmentEntityListener implements BeforeInsertEntityListener<CardAttachment>, BeforeDeleteEntityListener<CardAttachment> {
    public void onBeforeInsert(CardAttachment entity) {
        if (!BooleanUtils.isTrue(entity.getCard().getHasAttachments())) {
            entity.getCard().setHasAttachments(true);
            if(!PersistenceHelper.isNew(entity.getCard()))
                setHasAttachmentsInCard(entity.getCard(), true);
        }
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
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("update wf$Card c set c.hasAttachments = ?1 " +
                    "where c.id = ?2");
            query.setParameter(1, hasAttachments);
            query.setParameter(2, card);
            query.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }
}