/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:10:44
 *
 * $Id$
 */
package workflow.activity

import org.jbpm.api.activity.ActivityBehaviour
import org.jbpm.api.activity.ActivityExecution
import com.haulmont.workflow.core.entity.Card
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.PersistenceProvider

import com.haulmont.cuba.core.Locator

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.haulmont.workflow.core.entity.CardProc

import com.haulmont.workflow.core.app.NotificationMatrixAPI

public class CardActivity implements ActivityBehaviour {

  String observers
  String notificationState

  boolean delayedNotify = false

  NotificationMatrixAPI notificationMatrix;

  private Log log = LogFactory.getLog(CardActivity.class)

  public void execute(ActivityExecution execution) throws Exception {
    notificationMatrix = Locator.lookup(NotificationMatrixAPI.NAME);

    Card card = findCard(execution)

    notificationState = (card.getState() != null ? card.getState() :'') + '.' + execution.getActivityName()
    card.setState(execution.getActivityName())

    CardProc cp = card.procs.find { it.proc == card.proc }
    cp?.setState(card.state)

    if (!delayedNotify)
      notificationMatrix.notify(card, notificationState)
  }

  protected Card findCard(ActivityExecution execution) {
    String key = execution.getKey()
    UUID cardId
    try {
      cardId = UUID.fromString(key)
    } catch (Exception e) {
      throw new RuntimeException('Unable to get cardId', e)
    }
    EntityManager em = PersistenceProvider.getEntityManager()
    Card card = em.find(Card.class, cardId)
    if (card == null)
      throw new RuntimeException("Card not found: $cardId")
    return card
  }
}