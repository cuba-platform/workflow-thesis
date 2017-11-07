/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.TimerEntity;
import com.haulmont.workflow.core.timer.TimerAction;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Date;
import java.util.Map;

public interface TimerManagerAPI {

    String NAME = "workflow_TimerManager";

    void addTimer(Card card, ActivityExecution execution, Date dueDate,
                  Class<? extends TimerAction> taskClass, Map<String, String> taskParams);

    void removeTimers(ActivityExecution execution);

    void removeTimers(ActivityExecution execution, Assignment assignment);

    void removeTimers(String jbpmExecutionId);

    void processTimers();

    void processTimer(TimerEntity timer);
}
