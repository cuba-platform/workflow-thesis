/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 09.11.2009 15:47:12
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

public interface WorkflowDeployerMBean {

    String OBJECT_NAME = "haulmont.workflow:service=WfDeployer";

    void start();
}
