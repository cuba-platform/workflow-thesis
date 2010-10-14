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
import java.util.List;

@Service(NotificationMatrixService.NAME)
public class NotificationMatrixServiceBean implements NotificationMatrixService {
    @Inject
    private NotificationMatrixAPI notificationMatrixAPI;

    public void notify(Card card, String state) {
        notificationMatrixAPI.notifyByCard(card, state);
    }

    public void notify(Card card, String state, List<String> excludedRoles, String subject, String body, boolean mail, boolean tray) {
        notificationMatrixAPI.notifyByCard(card, state, excludedRoles, subject, body, mail, tray);
    }

    public void notify(Card card, String state, List<String> excludedRoles) {
        notificationMatrixAPI.notifyByCard(card, state, excludedRoles);
    }
}
