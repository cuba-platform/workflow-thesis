/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.workflow.core.app.design.DesignerWorkerAPI;
import com.haulmont.workflow.core.entity.Design;

import java.io.IOException;
import java.util.Collection;


/**
 * DEPRECATED - use {@link DesignerWorkerAPI} via DI or <code>AppBeans.get(DesignerWorkerAPI.class)</code>
 */
@Deprecated
public class DesignImportExportHelper {

    public static byte[] exportDesign(Design design) throws IOException, FileStorageException {
        return AppBeans.get(DesignerWorkerAPI.class).exportDesign(design);
    }

    public static byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException {
        return AppBeans.get(DesignerWorkerAPI.class).exportDesigns(designs);
    }

    public static Collection<Design> importDesigns(byte[] zipBytes) throws IOException, FileStorageException {
        return AppBeans.get(DesignerWorkerAPI.class).importDesigns(zipBytes);
    }

    public static Design importDesign(byte[] zipBytes, Boolean isArchive) throws IOException, FileStorageException {
        return AppBeans.get(DesignerWorkerAPI.class).importDesign(zipBytes, isArchive);
    }
}
