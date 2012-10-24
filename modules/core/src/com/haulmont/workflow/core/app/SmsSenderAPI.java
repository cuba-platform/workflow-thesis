/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.workflow.core.entity.SendingSms;

import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */

public interface SmsSenderAPI {
    String NAME = "workflow_SmsSender";

    SendingSms sendSmsSync(SendingSms sendingSms);

    void scheduledSendSms(SendingSms sendingSms) throws LoginException;

    SendingSms sendSmsAsync(String phone, String message);

    SendingSms sendSmsAsync(SendingSms sendingSms);

    List<SendingSms> sendSmsAsync(List<SendingSms> sendingSmsList);

    SendingSms checkStatus(SendingSms sendingSms);

    void scheduledCheckStatusSms(SendingSms sendingSms) throws LoginException;
}
