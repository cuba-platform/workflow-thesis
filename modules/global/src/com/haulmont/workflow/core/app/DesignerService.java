/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;

import java.io.IOException;
import java.util.*;

public interface DesignerService {

    String NAME = "workflow_DesignerService";

    /**
     * Creates an exact copy of a design with specified id.
     *
     * @param srcId source design id
     * @return design copy id
     */
    UUID copyDesign(UUID srcId);

    /**
     * Compiles a design with a specified id.
     *
     * @param designId design id
     * @return compilation message, that contains information about compilation
     *         errors and warnings
     * @throws DesignCompilationException
     */
    CompilationMessage compileDesign(UUID designId) throws DesignCompilationException;

    /**
     * Deploys a design with a specified id in application environment.
     *
     * @param designId design id
     * @param procId   workflow process described by this design
     * @param role     process will be available for this role only.
     * @throws DesignDeploymentException
     */
    void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException;

    /**
     * Creates an xls notification matrix from design notification
     * jpdl template and returns its byte content.
     *
     * @param designId design id
     * @return byte content of xls notification matrix
     * @throws TemplateGenerationException
     */
    byte[] getNotificationMatrixTemplate(UUID designId) throws TemplateGenerationException;

    /**
     * Compiles design messages for given locales.
     *
     * @param design    design
     * @param languages list of languages
     * @return map, where key is language string and value is message properties object
     * @throws DesignCompilationException
     */
    Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages) throws DesignCompilationException;

    /**
     * Exports a design into a byte array.
     *
     * @param design design
     * @return byte array design content
     * @throws IOException
     * @throws FileStorageException
     */
    byte[] exportDesign(Design design) throws IOException, FileStorageException;

    /**
     * Recovers a design from a byte array.
     *
     * @param bytes byte array content
     * @return recovered design
     * @throws IOException
     * @throws FileStorageException
     */
    Design importDesign(byte[] bytes) throws IOException, FileStorageException;

    /**
     * Exports given designs into a byte array.
     *
     * @param designs designs
     * @return designs byte array representation
     * @throws IOException
     * @throws FileStorageException
     */
    byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException;

    /**
     * Recovers designs from a byte array.
     *
     * @param bytes byte array content
     * @return designs
     * @throws IOException
     * @throws FileStorageException
     */
    Collection<Design> importDesigns(byte[] bytes) throws IOException, FileStorageException;

    /**
     * Stores a design notification matrix file in a database.
     *
     * @param design design
     */
    void saveNotificationMatrixFile(Design design);
}
