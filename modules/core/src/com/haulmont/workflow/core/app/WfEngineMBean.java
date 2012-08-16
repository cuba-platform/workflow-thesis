/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 10.11.2009 12:09:40
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface WfEngineMBean {

    String OBJECT_NAME = "haulmont.workflow:service=WfEngine";

    String JBPM_CFG_NAME_PROP = "cuba.jbpmCfgName";

    String DEF_JBPM_CFG_NAME = "wf.jbpm.cfg.xml";

    String getJbpmConfigName();

    @ManagedOperationParameters({@ManagedOperationParameter(name = "name", description = "")})
    String deployProcess(String name);

    String printDeployments();

    @ManagedOperationParameters({@ManagedOperationParameter(name = "id", description = "")})
    String printDeploymentResource(String id);

    String printProcessDefinitions();

    String deployTestProcesses();

    @ManagedOperationParameters({@ManagedOperationParameter(name = "key", description = "")})
    String startProcessByKey(String key);
}
