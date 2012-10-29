/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.Locator;
import com.haulmont.workflow.core.entity.SendingSms;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SmsSendTask implements Runnable {

    private SendingSms sendingSms;
    private Log log = LogFactory.getLog(SmsSendTask.class);

    public SmsSendTask(SendingSms message) {
        sendingSms = message;
    }

    @Override
    public void run() {
        try {
            SmsSenderAPI smsSender = Locator.lookup(SmsSenderAPI.NAME);
            smsSender.scheduledSendSms(sendingSms);
        } catch (Exception e) {
            log.error("Exception while sending sms " + sendingSms);
        }
    }
}