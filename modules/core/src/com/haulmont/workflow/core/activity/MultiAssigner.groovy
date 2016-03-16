/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import org.jbpm.api.activity.ActivityExecution

abstract class MultiAssigner extends Assigner {

  String successTransition
  def successTransitions = []
  Boolean refusedOnly

  protected boolean forRefusedOnly(ActivityExecution execution) {
    return refusedOnly || execution.getVariable("refusedOnly")
  }

    @Override
    void execute(ActivityExecution execution) {
        super.execute(execution)
    }

    public void setSuccessTransition(String value) {
        successTransition = value
        if (successTransition) {
            String[] parts = successTransition.split('[;,]')
            successTransitions.addAll(parts)
        }
    }

    protected List<CardRole> getCardRoles(ActivityExecution execution, Card card) {
    EntityManager em = AppBeans.get(Persistence.class).getEntityManager()
    Query q = em.createQuery('''
          select cr from wf$CardRole cr where cr.card.id = ?1 and cr.procRole.code = ?2 and cr.procRole.proc.id = ?3
          order by cr.sortOrder, cr.createTs
        ''')
    q.setParameter(1, card)
    q.setParameter(2, role)
    q.setParameter(3, card.proc)
    Collection<CardRole> cardRoles = q.getResultList()
    if (forRefusedOnly(execution)) {
      cardRoles = cardRoles.findAll { CardRole cr ->
        Query query = em.createQuery('''
          select a.outcome from wf$Assignment a
          where a.card.id = ?1 and a.user.id = ?2 and a.name = ?3 and a.finished is not null
          order by a.createTs
        ''')
        query.setParameter(1, card.id)
        query.setParameter(2, cr.user.id)
        query.setParameter(3, execution.activityName)
        List list = query.getResultList()
        if (list.isEmpty()) {
          return true
        } else {
          return !successTransitions.contains(list.last())
        }
      }
    }
    return cardRoles
  }
}
