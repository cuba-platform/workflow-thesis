/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.timer;

import com.haulmont.workflow.core.entity.Assignment;
import org.jbpm.api.activity.ActivityExecution;

/**
 * @author krivopustov
 * @version $Id$
 */
public interface AssignmentTimersFactory {

    void createTimers(ActivityExecution execution, Assignment assignment);

    void removeTimers(ActivityExecution execution);

    void removeTimers(ActivityExecution execution, Assignment assignment);
}