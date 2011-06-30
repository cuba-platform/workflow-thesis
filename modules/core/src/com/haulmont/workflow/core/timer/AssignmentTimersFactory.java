/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.01.2010 17:18:58
 *
 * $Id$
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.workflow.core.entity.Assignment;
import org.jbpm.api.activity.ActivityExecution;

public interface AssignmentTimersFactory {

    void createTimers(ActivityExecution execution, Assignment assignment);

    void removeTimers(ActivityExecution execution);

    void removeTimers(ActivityExecution execution, Assignment assignment);
}
