/*
 * Copyright (c) 2008-2020 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.reassignment;

import com.google.common.collect.Multimap;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardRole;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * @author stefanov
 */
public
interface CreateAssignmentForNewCardRolePredicate extends Serializable {
    boolean apply(CardRole newRole,
                  Multimap<User, Assignment> assignmentsMap,
                  List<CardRole> oldRoles,
                  HashMap<String, Object> additionalParams);
}
