/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 31.01.11 11:01
 *
 * $Id$
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
