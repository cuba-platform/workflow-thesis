/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;

import java.util.List;
import java.util.Map;

/**
 */
public interface NotificationMatrixAPI {
    String NAME = "workflow_NotificationMatrix";

    enum NotificationType {
        NO,
        SIMPLE,  //grey
        ACTION,  //green
        WARNING, //red
        REASSIGN,
        OVERDUE;

        public static NotificationType fromId(String id) {
            if (id == null) {
                return null;
            }

            NotificationType[] values = NotificationType.values();
            for (NotificationType notificationType : values) {
                if (id.equals(notificationType.toString())) {
                    return notificationType;
                }
            }

            return null;
        }
    }

    void notifyByCard(Card card, String state);

    void notifyByCard(Card card, String state, List<String> excludedRoles);

    void notifyByCard(Card card, String state, List<String> excludedRoles, NotificationMatrixMessage.MessageGenerator messageGenerator);

    void notifyByCardAndAssignments(Card card, Map<Assignment, CardRole> assignmentsCardRoleMap, String state);

    void notifyByCardAndAssignments(Card card, Map<Assignment, CardRole> assignmentsCardRoleMap, String state,
                                    List<String> extraExcludedRoles);

    void notifyUser(Card card, String state, User user);

    void notifyCardRole(Card card, CardRole cardRole, String state, Assignment assignment);

    void reload(String processPath) throws Exception;
}
