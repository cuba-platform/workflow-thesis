/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.workflow.core.WfHelper
import org.jbpm.api.listener.EventListener
import org.jbpm.api.listener.EventListenerExecution

/**
 *
 * @author stekolschikov
 * @version $Id$
 */
class RemoveTimersEventListener implements EventListener {

    @Override
    void notify(EventListenerExecution execution) throws Exception {
        WfHelper.getTimerManager().removeTimers(execution.getId());
    }
}
