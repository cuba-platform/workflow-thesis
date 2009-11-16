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

public interface WfEngineMBean {

    String OBJECT_NAME = "haulmont.workflow:service=WfEngine";

    String JBPM_CFG_NAME_PROP = "cuba.jbpmCfgName";

    String DEF_JBPM_CFG_NAME = "META-INF/wf.jbpm.cfg.xml";

    WfEngineAPI getAPI();

    String getJbpmConfigName();

    String deployJpdlXml(String fileName);

    String printDeployments();

    String printDeploymentResource(String id);

    String printProcessDefinitions();
}
