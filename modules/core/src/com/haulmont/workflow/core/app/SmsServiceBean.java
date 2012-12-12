/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;


/**
 * <p>$Id$</p>
 *
 * @author novikov
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
