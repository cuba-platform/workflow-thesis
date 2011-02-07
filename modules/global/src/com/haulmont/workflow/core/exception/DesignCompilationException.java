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
