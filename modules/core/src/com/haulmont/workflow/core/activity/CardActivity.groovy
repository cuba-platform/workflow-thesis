/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:10:44
 *
 * $Id$
 */
package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.Locator
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardProc
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.Execution
import org.jbpm.api.activity.ActivityExecution

public class CardActivity extends ProcessVariableActivity {

  public static String PREV_ACTIVITY_VAR_NAME = 'prevActivityName';
  String observers
  String notificationState

  boolean delayedNotify = false

  NotificationMatrixAPI notificationMatrix;

  private Log log = LogFactory.getLog(CardActivity.class)

  public void execute(ActivityExecution execution) throws Exception {
    super.execute(execution);

    notificationMatrix = AppBeans.get(NotificationMatrixAPI.NAME);

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
    return ActivityHelper.findCard(execution)
  }

  protected void afterSignal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    Card card = findCard(execution);
    card.state = card.state - "${execution.getActivityName()},"
    execution.createVariable(PREV_ACTIVITY_VAR_NAME, execution.getActivityName())
  }
}