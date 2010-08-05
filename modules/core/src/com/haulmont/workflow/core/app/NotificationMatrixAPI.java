/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 29.07.2010 15:16:31
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;

public interface NotificationMatrixAPI {
    String NAME = "workflow_NotificationMatrix";

    public enum NotificationType {
        NO,
        SIMPLE,
        ACTION;

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
    
    void notifyByCard(Card card, String state, String excludedRole);

    void notifyByAssignment(Assignment assignment, CardRole cardRole, String state);
}
