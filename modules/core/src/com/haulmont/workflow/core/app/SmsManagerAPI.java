/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;


import com.haulmont.workflow.core.entity.SendingSms;

import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public interface SmsManagerAPI {
    String NAME = "workflow_SmsManager";

    List<SendingSms> addSmsToQueue(List<SendingSms> sendingSmsList);

    void queueSmsToSend();
}
