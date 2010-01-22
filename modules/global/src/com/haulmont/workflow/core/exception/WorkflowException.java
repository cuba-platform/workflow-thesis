/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 08.01.2010 16:02:17
 *
 * $Id$
 */
package com.haulmont.workflow.core.exception;

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
