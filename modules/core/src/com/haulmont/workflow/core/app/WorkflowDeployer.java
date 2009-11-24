/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 09.11.2009 15:48:15
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.app.ServerConfig;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.global.ConfigProvider;

public class WorkflowDeployer implements WorkflowDeployerMBean {

    static {
        System.setProperty(PersistenceProvider.PERSISTENCE_UNIT, "workflow");
        System.setProperty(PersistenceProvider.PERSISTENCE_XML, "META-INF/workflow-persistence.xml");
    }


    public void start() {
        ServerConfig config = ConfigProvider.getConfig(ServerConfig.class);
        ScriptingProvider.addGroovyClassPath(config.getServerConfDir() + "/workflow");
    }
}
