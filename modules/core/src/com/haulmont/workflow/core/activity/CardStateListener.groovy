/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.activity

import org.jbpm.api.listener.EventListenerExecution
import org.jbpm.api.activity.ActivityExecution

class CardStateListener implements org.jbpm.api.listener.EventListener {

  void notify(EventListenerExecution execution) {
    execution.createVariable(CardActivity.PREV_ACTIVITY_VAR_NAME, ((ActivityExecution)execution).getActivityName())
  }
}
