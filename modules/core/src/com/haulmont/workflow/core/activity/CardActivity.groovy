/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardProc
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.Execution
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.jpdl.internal.activity.ForkActivity
import org.jbpm.pvm.internal.model.ActivityImpl

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
        initializeNotificationState(execution);
        StringBuilder sb = new StringBuilder(',')
        //find all current executions
        def executions = execution.getIsProcessInstance() ? [execution] : execution.getProcessInstance().getExecutions()
        executions.each { ActivityExecution childExecution ->
            def activityBehaviour = ((ActivityImpl) childExecution.getActivity()).getActivityBehaviour()
            if (!(activityBehaviour instanceof ForkActivity) &&
                    (childExecution.state == Execution.STATE_ACTIVE_CONCURRENT)
                    || (childExecution.state == Execution.STATE_ACTIVE_ROOT))
                sb.append(childExecution.getActivityName()).append(',')
        }
        card.state = sb.toString()

        CardProc cp = card.procs.find { it.proc == card.proc }
        cp?.setState(card.state)
        if (!delayedNotify)
            notificationMatrix.notifyByCard(card, getNotificationState(execution))
    }

    protected String getNotificationState(ActivityExecution execution) {
        if (!notificationState)
            initializeNotificationState(execution)
        return notificationState
    }

    protected void initializeNotificationState(ActivityExecution execution) {
        def prevActivityName = execution.getVariable(PREV_ACTIVITY_VAR_NAME)
        notificationState = (prevActivityName ? prevActivityName + '.' : '') + execution.getActivityName()
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