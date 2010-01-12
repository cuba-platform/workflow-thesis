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

public class WorkflowException extends RuntimeException{
    private static final long serialVersionUID = 5636812930224275069L;

    public WorkflowException(String message) {
        super(message);
    }
}
