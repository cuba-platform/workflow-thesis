/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.exception;

public class TransientSmsException extends SmsException {
    public TransientSmsException(int code, String msg) {
        super(code, msg);
    }
}
