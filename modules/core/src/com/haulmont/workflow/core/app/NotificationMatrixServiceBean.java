/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 02.08.2010 14:37:36
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import org.springframework.stereotype.Service;

import javax.inject.Inject;

@Service(NotificationMatrixService.NAME)
public class NotificationMatrixServiceBean implements NotificationMatrixService {
    @Inject
    private NotificationMatrixAPI notificationMatrixAPI;

    public void notify(Card card, String state) {
        notificationMatrixAPI.notify(card, state);
    }

    public void notify(Card card, String state, String excludedRole) {
        notificationMatrixAPI.notify(card, state, excludedRole);
    }
}
