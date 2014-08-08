/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Category;
import com.haulmont.cuba.core.listener.BeforeDeleteEntityListener;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.cuba.core.listener.BeforeUpdateEntityListener;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.TimerEntity;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.List;

/**
 * @author devyatkin
 * @version $Id$
 */
@ManagedBean("workflow_CardListener")
public class CardListener implements
        BeforeDeleteEntityListener<Card>,
        BeforeUpdateEntityListener<Card>,
        BeforeInsertEntityListener<Card> {

    @Inject
    protected Persistence persistence;

    @Override
    public void onBeforeDelete(Card card) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery();
        query.setQueryString("select t from wf$Timer t where t.card.id=:id");
        query.setParameter("id", card.getId());
        List<TimerEntity> timers = query.getResultList();
        for (TimerEntity timer : timers) {
            em.remove(timer);
        }
    }

    @Override
    public void onBeforeInsert(Card card) {
        setHasAttributesForCard(card);
    }

    @Override
    public void onBeforeUpdate(Card card) {
        setHasAttributesForCard(card);
    }

    private void setHasAttributesForCard(Card card) {
        Category c = getCategory(card);
        if (c != null && c.getCategoryAttrs() != null && c.getCategoryAttrs().size() > 0)
            card.setHasAttributes(true);
        else
            card.setHasAttributes(false);
    }

    private Category getCategory(Card card) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            em.setSoftDeletion(false);
            Card c = em.find(Card.class, card.getId());
            Category category = null;
            if (c.getCategory() != null) {
                category = em.find(Category.class, c.getCategory().getId());
            }
            em.setSoftDeletion(true);
            tx.commit();
            return category;
        } finally {
            tx.end();
        }
    }
}