/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.exception;

public class TemplateGenerationException extends Exception {
    public TemplateGenerationException(String message) {
        super(message);
    }

    public TemplateGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemplateGenerationException(Throwable cause) {
        super(cause);
    }
}
