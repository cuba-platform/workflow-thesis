/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

/**
 * @author pavlov
 * @version $Id$
 */
@Service(NotificationMatrixService.NAME)
public class NotificationMatrixServiceBean implements NotificationMatrixService {
    @Inject
    private NotificationMatrixAPI notificationMatrixAPI;

    @Override
    public void notify(Card card, String state) {
        notificationMatrixAPI.notifyByCard(card, state);
    }

    @Override
    public void notify(Card card, String state, List<String> excludedRoles) {
        notificationMatrixAPI.notifyByCard(card, state, excludedRoles);
    }

    @Override
    public void notify(Card card, String state, List<String> excludedRoles, NotificationMatrixMessage.MessageGenerator messageGenerator) {
        notificationMatrixAPI.notifyByCard(card, state, excludedRoles, messageGenerator);
    }
}