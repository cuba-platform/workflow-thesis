/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 12:14:01
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.cuba.core.Locator;
import org.jbpm.api.ProcessEngine;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.RepositoryService;

public class WfHelper {

    public static WfEngineAPI getEngine() {
        WfEngineAPI mbean = Locator.lookup(WfEngineAPI.NAME);
        return mbean;
    }

    public static ProcessEngine getProcessEngine() {
        return getEngine().getProcessEngine();
    }

    public static ExecutionService getExecutionService() {
        return getEngine().getProcessEngine().getExecutionService();
    }

    public static RepositoryService getRepositoryService() {
        return getEngine().getProcessEngine().getRepositoryService();
    }
}
