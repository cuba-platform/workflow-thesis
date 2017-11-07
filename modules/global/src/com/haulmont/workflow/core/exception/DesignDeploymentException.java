/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.exception;

public class DesignDeploymentException extends Exception {

    public DesignDeploymentException(String message) {
        super(message);
    }

    public DesignDeploymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public DesignDeploymentException(Throwable cause) {
        super(cause);
    }
}
