/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */



package com.haulmont.workflow.core.activity

import com.haulmont.workflow.core.entity.Card
import org.jbpm.api.listener.EventListenerExecution

/**
 *
 */
class StartProcessListener implements org.jbpm.api.listener.EventListener {
    @Override
    void notify(EventListenerExecution execution) {
        Card card = com.haulmont.workflow.core.activity.ActivityHelper.findCard(execution);
        if (card != null && execution.getIsProcessInstance()) {
            card.setJbpmProcessId(execution.id);
        }
    }
}
