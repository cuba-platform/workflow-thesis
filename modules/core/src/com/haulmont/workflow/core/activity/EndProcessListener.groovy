/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardProc
import com.haulmont.workflow.core.global.WfConstants
import org.apache.commons.lang.StringUtils
import org.jbpm.api.listener.EventListenerExecution
import org.jbpm.pvm.internal.model.ExecutionImpl

import javax.annotation.Nullable

class EndProcessListener implements org.jbpm.api.listener.EventListener {

    @Override
    void notify(EventListenerExecution execution) {
        NotificationMatrixAPI notificationMatrix = AppBeans.get(NotificationMatrixAPI.NAME);
        Card card = ActivityHelper.findCard(execution);
        for (CardProc cp in card.procs) {
            cp.active = false;
        }
        card.jbpmProcessId = null;

        if (!WfConstants.CARD_STATE_CANCELED.equals(execution.state)) {
            String nextActivityName = getNextActivityName(execution)
            if (StringUtils.isNotBlank(nextActivityName)) {
                String prevActivityName = execution.getVariable("prevActivityName")
                notificationMatrix.notifyByCard(card, prevActivityName + "." + nextActivityName);
            }
        }
    }

    @Nullable
    protected String getNextActivityName(EventListenerExecution execution) {
        if (execution instanceof ExecutionImpl) {
            return execution.transition?.destination?.name;
        }
        return null;
    }
}
