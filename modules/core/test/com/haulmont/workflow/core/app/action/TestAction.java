/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 12.11.2009 12:39:39
 *
 * $Id$
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
