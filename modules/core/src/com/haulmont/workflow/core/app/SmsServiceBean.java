/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


/**
 *
 */
@Service(SmsService.NAME)
public class SmsServiceBean implements SmsService {

    protected static final Log log = LogFactory.getLog(SmsServiceBean.class);

    @Inject
    protected SmsSenderAPI smsSender;

    @Override
    public void sendSms(String phone, String addressee, String message) {
        smsSender.sendSmsAsync(phone, addressee, message);
    }
}
