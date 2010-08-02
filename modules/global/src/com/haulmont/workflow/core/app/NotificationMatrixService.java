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

public interface NotificationMatrixService {
    String NAME = "workflow_NotificationMatrixService";

    void notify(Card card, String state);

    void notify(Card card, String state, String excludedRole);
}
