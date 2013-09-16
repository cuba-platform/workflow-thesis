/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.jmx;

import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;

public interface WfEngineMBean {

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
