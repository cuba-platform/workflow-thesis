/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.11.2009 18:10:27
 *
 * $Id$
 */
package com.haulmont.workflow.core.activity;

import org.jbpm.api.activity.ActivityBehaviour;
import org.jbpm.api.activity.ActivityExecution;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.PersistenceProvider;

import java.util.UUID;

public class CardActivity implements ActivityBehaviour {

    public void execute(ActivityExecution execution) throws Exception {
        Card card = findCard(execution);
        card.setState(execution.getActivityName());
    }

    protected Card findCard(ActivityExecution execution) {
        String key = execution.getKey();
        UUID cardId;
        try {
            cardId = UUID.fromString(key);
        } catch (Exception e) {
            throw new RuntimeException("Unable to get cardId", e);
        }
        EntityManager em = PersistenceProvider.getEntityManager();
        Card card = em.find(Card.class, cardId);
        if (card == null)
            throw new RuntimeException("Card not found: " + cardId);
        return card;
    }
}
