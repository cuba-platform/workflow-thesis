/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Devyatkin
 * Created: ${DATE} ${TIME}
 *
 * $Id$
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
