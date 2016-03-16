/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 */
public interface DesignerWorkerAPI {
    String NAME = "workflow_DesignerWorker";

    CompilationMessage compileDesign(UUID designId) throws DesignCompilationException;

    void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException;

    byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException;

    byte[] exportDesign(Design design) throws IOException, FileStorageException;

    Design importDesign(byte[] bytes) throws IOException, FileStorageException;

    Collection<Design> importDesigns(byte[] zipBytes) throws IOException, FileStorageException;

    Design importDesign(byte[] zipBytes, Boolean isArchive) throws IOException, FileStorageException;
}
