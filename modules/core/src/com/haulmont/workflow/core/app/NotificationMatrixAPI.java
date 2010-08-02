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

    void notify(Card card, String state);
    
    void notify(Card card, String state, String excludedRole);

    void notify(Card card, String state, Assignment assignment, String assignmentRole);
}
