/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Gennady Pavlov
 * Created: 02.08.2010 14:39:02
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;

import java.util.List;

public interface NotificationMatrixService {
    String NAME = "workflow_NotificationMatrixService";

    /**
     * Notifies process actors about new card state transition in a workflow process.
     *
     * @param card  card in a workflow process
     * @param state card process state
     */
    void notify(Card card, String state);

    /**
     * Same as <code>NotificationMatrixService.notify(Card card, String state)</code>, but
     * do not notifies actors with specified excluded process roles.
     *
     * @param card          card in a workflow process
     * @param state         card process state
     * @param excludedRoles process roles, that won't be notified
     */
    void notify(Card card, String state, List<String> excludedRoles);

    /**
     * Same as <code>NotificationMatrixService.notify(Card card, String state, List<String> excludedRoles</code>,
     * but allows to specify {@link NotificationMatrixMessage.MessageGenerator} instead of using
     * <code>NotificationMatrix.DefaultMessageGenerator</code>.
     *
     * @param card             card in a workflow process
     * @param state            card process state
     * @param excludedRoles    process roles, that won't be notified
     * @param messageGenerator notification message generator
     */
    void notify(Card card, String state, List<String> excludedRoles, NotificationMatrixMessage.MessageGenerator messageGenerator);
}
