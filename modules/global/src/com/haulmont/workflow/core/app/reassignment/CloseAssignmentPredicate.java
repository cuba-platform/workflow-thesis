/*
 * Copyright (c) 2008-2020 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.reassignment;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

/**
 * @author stefanov
 */
public interface CloseAssignmentPredicate extends Serializable {
    boolean apply(Collection<? extends User> newRoleUsers,
                  Assignment assignmentToClose,
                  HashMap<String, Object> additionalParams);
}
