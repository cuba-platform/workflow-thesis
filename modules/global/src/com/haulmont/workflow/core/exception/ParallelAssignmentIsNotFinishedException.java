/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.exception;

import java.util.UUID;

/**
 */
public class ParallelAssignmentIsNotFinishedException extends Exception {
    protected UUID assignmentId;

    public ParallelAssignmentIsNotFinishedException(UUID assignmentId) {
        this.assignmentId = assignmentId;
    }

    public UUID getAssignmentId() {
        return assignmentId;
    }
}
