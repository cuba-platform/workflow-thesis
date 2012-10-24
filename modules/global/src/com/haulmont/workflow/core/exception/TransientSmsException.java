/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.exception;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class TransientSmsException extends SmsException {
    public TransientSmsException(int code, String msg) {
        super(code, msg);
    }
}
