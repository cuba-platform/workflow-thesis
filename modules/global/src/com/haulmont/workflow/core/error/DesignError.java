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
public class DesignError implements DesignCompilationError {
    private String message;

    public DesignError(String message) {
        this.message = message;

    }

    public String getMessage() {
        return message;
    }
}
