/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.app.TimerManagerAPI;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author krivopustov
 * @version $Id$
 */
@ManagedBean("workflow_TimerManagerMBean")
public class TimerManager implements TimerManagerMBean {

    @Inject
    protected TimerManagerAPI timerManager;

    @Override
    public void processTimers() {
        timerManager.processTimers();
    }
}
