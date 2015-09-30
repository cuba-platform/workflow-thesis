/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.app.TimerManagerAPI;

import org.springframework.stereotype.Component;
import javax.inject.Inject;

/**
 * @author krivopustov
 * @version $Id$
 */
@Component("workflow_TimerManagerMBean")
public class TimerManager implements TimerManagerMBean {

    @Inject
    protected TimerManagerAPI timerManager;

    @Override
    public void processTimers() {
        timerManager.processTimers();
    }
}
