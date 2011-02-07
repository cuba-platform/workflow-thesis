/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 13:47
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public interface DesignerService {

    String NAME = "workflow_DesignerService";

    UUID copyDesign(UUID srcId);
    
    void compileDesign(UUID designId) throws DesignCompilationException;

    void deployDesign(UUID designId, UUID procId) throws DesignDeploymentException;

    Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages) throws DesignCompilationException;
}
