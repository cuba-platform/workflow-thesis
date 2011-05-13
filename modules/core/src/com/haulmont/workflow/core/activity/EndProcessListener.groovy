/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 04.02.11 16:32
 *
 * $Id$
 */

package com.haulmont.workflow.core.activity

import org.jbpm.api.listener.EventListenerExecution
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardProc

class EndProcessListener implements org.jbpm.api.listener.EventListener {

  void notify(EventListenerExecution execution) {
    Card card = com.haulmont.workflow.core.activity.ActivityHelper.findCard(execution);
    for (CardProc cp in card.procs) {
      cp.active = false;
    }
  }

}
