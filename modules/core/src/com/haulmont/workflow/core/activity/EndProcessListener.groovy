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

import com.haulmont.cuba.core.global.AppBeans
import org.jbpm.api.listener.EventListenerExecution
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardProc
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.cuba.core.Locator
import org.jbpm.api.activity.ActivityExecution

class EndProcessListener implements org.jbpm.api.listener.EventListener {

    void notify(EventListenerExecution execution) {
        NotificationMatrixAPI notificationMatrix = AppBeans.get(NotificationMatrixAPI.NAME);
        Card card = com.haulmont.workflow.core.activity.ActivityHelper.findCard(execution);
        for (CardProc cp in card.procs) {
            cp.active = false;
        }
        card.jbpmProcessId = null;

        if (!"Canceled".equals(execution.state)) {
            String activityName = ((ActivityExecution) execution).getActivityName();
            String prevActivityName = execution.getVariable("prevActivityName")
            notificationMatrix.notifyByCard(card, prevActivityName + "." + activityName);
        }
    }

}
