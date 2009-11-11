/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 11.11.2009 18:43:05
 *
 * $Id$
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.CubaTestCase;
import com.haulmont.cuba.core.Locator;
import org.jbpm.api.Configuration;
import org.jbpm.api.ProcessEngine;

public abstract class WfTestCase extends CubaTestCase {

    @Override
    protected void beforeInitEjb() throws Exception {
        ProcessEngine processEngine = new Configuration().buildProcessEngine();
    }
}
