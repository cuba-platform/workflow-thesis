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
public class DesignError implements DesignCompilationError {
    private String message;

    public DesignError(String message) {
        this.message = message;

    }

    public String getMessage() {
        return message;
    }
}
