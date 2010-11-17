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
import org.jbpm.api.Execution
import java.util.regex.Pattern
import java.util.regex.Matcher

public class CardActivity implements ActivityBehaviour {

  public static String PREV_ACTIVITY_VAR_NAME = 'prevActivityName';
  String observers
  String notificationState

  boolean delayedNotify = false

  NotificationMatrixAPI notificationMatrix;

  private Log log = LogFactory.getLog(CardActivity.class)

  public void execute(ActivityExecution execution) throws Exception {
    notificationMatrix = Locator.lookup(NotificationMatrixAPI.NAME);

    Card card = findCard(execution)

    String prevActivityName = execution.getVariable(PREV_ACTIVITY_VAR_NAME)
    notificationState = (prevActivityName != null ? prevActivityName + '.' :'') + execution.getActivityName()
    StringBuilder sb = new StringBuilder(',')
    //find all current executions
    def executions = execution.getIsProcessInstance() ? [execution] : execution.getProcessInstance().getExecutions()
    executions.each{ActivityExecution childExecution ->
      if ((childExecution.state == Execution.STATE_ACTIVE_CONCURRENT)
        || (childExecution.state == Execution.STATE_ACTIVE_ROOT))
      sb.append(childExecution.getActivityName()).append(',')
    }
    card.state = sb.toString()

    CardProc cp = card.procs.find { it.proc == card.proc }
    cp?.setState(card.state)

    if (!delayedNotify)
      notificationMatrix.notifyByCard(card, notificationState)
  }

  protected Card findCard(ActivityExecution execution) {
    String key = null;

//    In case of parallel executions execution object does not have
//    filled key property. We must extract execution_key from id.
//    id format : {process_key}.{execution_key}.{id}
    if (execution.state == ActivityExecution.STATE_ACTIVE_CONCURRENT) {
      Matcher matcher = Pattern.compile("\\.(.*)\\.").matcher(execution.getId());
      if (matcher.find())
        key = matcher.group(1);
    } else {
      key = execution.getKey()
    }

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

  protected void afterSignal(ActivityExecution execution) {
    Card card = findCard(execution);
    card.state = card.state - "${execution.getActivityName()},"
    execution.createVariable(PREV_ACTIVITY_VAR_NAME, execution.getActivityName())
  }
}