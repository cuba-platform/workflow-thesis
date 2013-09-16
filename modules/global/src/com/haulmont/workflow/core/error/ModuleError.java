/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.error;


/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public class ModuleError implements DesignCompilationError {
    private String message;
    private String moduleName;

    public ModuleError(String moduleName, String message) {
        this.moduleName = moduleName;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getModuleName() {
        return moduleName;
    }
}
