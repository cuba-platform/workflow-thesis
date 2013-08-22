/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public interface DesignerWorkerAPI {
    String NAME = "workflow_DesignerWorker";

    CompilationMessage compileDesign(UUID designId) throws DesignCompilationException;

    void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException;

    byte[] exportDesign(Design design) throws IOException, FileStorageException;

    Design importDesign(byte[] bytes) throws IOException, FileStorageException;
}
