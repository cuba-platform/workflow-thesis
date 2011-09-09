/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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
