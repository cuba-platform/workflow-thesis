/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.exception;

import com.haulmont.cuba.core.global.SupportedByClient;

/**
 * Raised in case of various workflow errors.
 *
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
@SupportedByClient
public class WorkflowException extends RuntimeException {

    private static final long serialVersionUID = 5636812930224275069L;

    private Type type;
    private Object[] params;

    public enum Type {
        UNDEFINED,
        NO_ACTIVE_EXECUTION,
        NO_CARD_ROLE
    }

    public WorkflowException(Type type, String message, Object... params) {
        super(message);
        this.type = type;
        this.params = params;
    }

    public WorkflowException(String message) {
        this(Type.UNDEFINED, message);
    }

    public Type getType() {
        return type;
    }

    public Object[] getParams() {
        return params;
    }
}
