/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.01.2010 10:33:18
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.timer.TimerAction;
import org.jbpm.api.activity.ActivityExecution;

import java.util.Date;
import java.util.Map;

public interface TimerManagerAPI {

    String NAME = "workflow_TimerManager";

    void addTimer(Card card, ActivityExecution execution, Date dueDate,
                  Class<? extends TimerAction> taskClass, Map<String, String> taskParams);

    void removeTimers(ActivityExecution execution);
}
