/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 12:10:36
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import org.jbpm.api.ProcessEngine;

public interface WfEngineAPI {

    ProcessEngine getProcessEngine();
}
