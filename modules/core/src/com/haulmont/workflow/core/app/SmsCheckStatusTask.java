/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.SendingSms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SmsCheckStatusTask implements Runnable {

    private SendingSms sendingSms;
    private Log log = LogFactory.getLog(SmsCheckStatusTask.class);

    public SmsCheckStatusTask(SendingSms message) {
        sendingSms = message;
    }

    @Override
    public void run() {
        try {
            SmsSenderAPI smsSender = AppBeans.get(SmsSenderAPI.NAME);
            smsSender.scheduledCheckStatusSms(sendingSms);
        } catch (Exception e) {
            log.error("Exception while check status sms " + sendingSms);
        }
    }
}
