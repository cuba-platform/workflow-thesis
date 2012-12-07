/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.workflow.core.app.ProcStageManagerAPI;
import org.apache.commons.lang.exception.ExceptionUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author krivopustov
 * @version $Id$
 */
@ManagedBean("workflow_ProcStageManagerMBean")
public class ProcStageManager implements ProcStageManagerMBean {

    @Inject
    protected ProcStageManagerAPI manager;

    @Override
    public String processOverdueStages() {
        try {
            manager.processOverdueStages();
            return "Done";
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }
}
