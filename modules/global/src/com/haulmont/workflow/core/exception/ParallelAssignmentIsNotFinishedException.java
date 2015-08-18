/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.exception;

import java.util.UUID;

/**
 * @author stekolschikov
 * @version $Id$
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
