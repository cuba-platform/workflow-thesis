/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.TimerEntity;
import com.haulmont.workflow.core.timer.AssignmentTimersFactory;
import com.haulmont.workflow.core.timer.TimerAction;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Date;
import java.util.Map;

public interface TimerManagerAPI {

    String NAME = "workflow_TimerManager";

    void addTimer(Card card, ActivityExecution execution, Date dueDate,
                  Class<? extends AssignmentTimersFactory> timersFactoryClass, Class<? extends TimerAction> taskClass,
                  Map<String, String> taskParams);

    void removeTimers(ActivityExecution execution);

    void removeTimers(ActivityExecution execution, Assignment assignment);

    void removeTimers(String jbpmExecutionId);

    void processTimers();

    void processTimer(TimerEntity timer);
}
