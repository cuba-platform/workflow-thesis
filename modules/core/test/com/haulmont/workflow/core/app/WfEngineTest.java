/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 13:45:21
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.CubaTestCase;
import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.WfTestCase;
import org.jbpm.api.ProcessEngine;

public class WfEngineTest extends WfTestCase {

    public void testGetProcessEngine() {
        WfEngineMBean mBean = Locator.lookupMBean(WfEngineMBean.class, WfEngineMBean.OBJECT_NAME);
        WfEngineAPI wfe = mBean.getAPI();
        ProcessEngine processEngine = wfe.getProcessEngine();
        assertNotNull(processEngine);
    }
}
