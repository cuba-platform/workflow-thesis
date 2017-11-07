/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.SendingSms;

import java.util.List;

/**
 */

public interface SmsSenderAPI {
    String NAME = "workflow_SmsSender";

    SendingSms sendSmsSync(SendingSms sendingSms);

    void scheduledSendSms(SendingSms sendingSms);

    SendingSms sendSmsAsync(String phone, String addressee, String message);

    SendingSms sendSmsAsync(SendingSms sendingSms);

    List<SendingSms> sendSmsAsync(List<SendingSms> sendingSmsList);

    SendingSms checkStatus(SendingSms sendingSms);

    void scheduledCheckStatusSms(SendingSms sendingSms);

    SmsProvider getSmsProvider();

    void setSmsProviderClassName(String value);

    void setUseDefaultSmsSending(boolean value);
}
