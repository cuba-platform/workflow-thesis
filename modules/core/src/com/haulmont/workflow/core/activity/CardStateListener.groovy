/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 16.11.2010 10:11:50
 *
 * $Id$
 */
package com.haulmont.workflow.core.activity

import org.jbpm.api.listener.EventListenerExecution
import org.jbpm.api.activity.ActivityExecution

class CardStateListener implements org.jbpm.api.listener.EventListener {

  void notify(EventListenerExecution execution) {
    execution.createVariable(CardActivity.PREV_ACTIVITY_VAR_NAME, ((ActivityExecution)execution).getActivityName())
  }
}
