/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.exception;

public class DesignCompilationException extends Exception {

    public DesignCompilationException(String message) {
        super(message);
    }

    public DesignCompilationException(String message, Throwable cause) {
        super(message, cause);
    }

    public DesignCompilationException(Throwable cause) {
        super(cause);
    }
}
