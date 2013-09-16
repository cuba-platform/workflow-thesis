/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.action;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class TestAction {

    private Log log = LogFactory.getLog(TestAction.class);
    
    public void run() {
        log.info("running TestAction");
    }
}
