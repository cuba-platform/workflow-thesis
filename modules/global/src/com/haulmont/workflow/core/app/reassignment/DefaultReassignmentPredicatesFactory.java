/*
 * Copyright (c) 2008-2020 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.reassignment;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardRole;
import org.apache.commons.lang.ObjectUtils;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author stefanov
 */
@SuppressWarnings("WeakerAccess")
public class DefaultReassignmentPredicatesFactory implements WfReassignmentPredicatesFactory {

    @Override
    public CreateAssignmentForNewCardRolePredicate getCreateAssignmentForNewCardRolePredicate() {
        return CREATE_ASSIGNMENT_FOR_NEW_CARD_ROLE_PREDICATE;
    }

    @Override
    public CloseAssignmentPredicate getCloseAssignmentPredicate() {
        return DEFAULT_CLOSE_ASSIGNMENT_PREDICATE;
    }

    protected static class DefaultCreateAssignmentForNewCardRolePredicate implements CreateAssignmentForNewCardRolePredicate {
        @Override
        public boolean apply(CardRole newRole,
                             Multimap<User, Assignment> assignmentsMap,
                             List<CardRole> oldRoles,
                             HashMap<String, Object> additionalParams) {
            if (assignmentsMap.containsKey(newRole.getUser())) {
                final Assignment lastAssignment = Collections.max(assignmentsMap.get(newRole.getUser()), BY_CREATE_TS_COMPARATOR);
                Predicate<CardRole> predicate = new Predicate<CardRole>() {
                    @Override
                    public boolean apply(@Nullable CardRole input) {
                        return input != null && ObjectUtils.equals(lastAssignment.getUser(), input.getUser());
                    }
                };
                return lastAssignment.getFinished() != null && !Iterables.any(oldRoles, predicate);
            } else {
                return newRole.getUser() != null;
            }
        }
    }

    protected static class DefaultCloseAssignmentPredicate implements CloseAssignmentPredicate{
        @Override
        public boolean apply(Collection<? extends User> newRoleUsers,
                             Assignment assignmentToClose,
                             HashMap<String, Object> additionalParams) {
            return !newRoleUsers.contains(assignmentToClose.getUser()) && assignmentToClose.getFinished() == null;
        }
    }

    protected static Comparator<Assignment> BY_CREATE_TS_COMPARATOR = new Comparator<Assignment>() {
        @Override
        public int compare(Assignment a1, Assignment a2) {
            if (a1.getCreateTs() == null && a2.getCreateTs() == null) {
                return 0;
            }
            if (a1.getCreateTs() == null) {
                return -1;
            }
            if (a2.getCreateTs() == null) {
                return 1;
            }
            return a1.getCreateTs().compareTo(a2.getCreateTs());
        }
    };

    protected static final CreateAssignmentForNewCardRolePredicate CREATE_ASSIGNMENT_FOR_NEW_CARD_ROLE_PREDICATE =
            new DefaultCreateAssignmentForNewCardRolePredicate();

    protected static final DefaultCloseAssignmentPredicate DEFAULT_CLOSE_ASSIGNMENT_PREDICATE =
            new DefaultCloseAssignmentPredicate();
}
