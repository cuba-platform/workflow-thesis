/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.DesignImportExportHelper;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
@ManagedBean(DesignerWorkerAPI.NAME)
public class DesignerWorker implements DesignerWorkerAPI {

    @Inject
    private DesignCompiler compiler;

    @Inject
    private DesignDeployer deployer;

    @Inject
    private ProcessMigrator migrator;

    @Override
    public CompilationMessage compileDesign(UUID designId) throws DesignCompilationException {
        return compiler.compileDesign(designId);
    }

    @Override
    public void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException {
        ProcessMigrator.Result result = null;
        if (procId != null) {
            result = migrator.checkMigrationPossibility(designId, procId);
            if (!result.isSuccess())
                throw new DesignDeploymentException(result.getMessage());
        }

        deployer.deployDesign(designId, procId, role);

        if (result != null && result.getOldJbpmProcessKey() != null) {
            migrator.migrate(designId, procId, result.getOldJbpmProcessKey());
        }
    }

    @Override
    public byte[] exportDesign(Design design) throws IOException, FileStorageException {
        return DesignImportExportHelper.exportDesigns(Arrays.asList(design));
    }

    @Override
    public Design importDesign(byte[] bytes) throws IOException, FileStorageException {
        return DesignImportExportHelper.importDesigns(bytes).iterator().next();
    }
}
