/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.exception;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SmsException extends Exception {

    private int code;

    public SmsException(int code, String msg) {
        super(code + ": " + msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
