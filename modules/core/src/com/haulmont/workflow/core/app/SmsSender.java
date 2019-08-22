/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;
import com.haulmont.workflow.core.exception.SmsException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Component(SmsSenderAPI.NAME)
public class SmsSender implements SmsSenderAPI {

    protected static final Log log = LogFactory.getLog(SmsSender.class);

    @Inject
    protected SmsManagerAPI smsManager;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected TimeSource timeSource;

    protected SmsSenderConfig config;

    protected volatile SmsProvider smsProvider;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    @Override
    public SmsProvider getSmsProvider() {
        if (config.getUseDefaultSmsSending()) {
            if (!(smsProvider instanceof DefaultSmsProvider)) {
                smsProvider = new DefaultSmsProvider();
            }
        } else {
            if (smsProvider == null || !smsProvider.getClass().getName().equals(config.getSmsProviderClassName())) {
                String className = config.getSmsProviderClassName();
                if (StringUtils.isNotBlank(className)) {
                    try {
                        smsProvider = (SmsProvider) Class.forName(className).newInstance();
                    } catch (Exception e) {
                        log.error(String.format("Failed create class '%s': %s", className, ExceptionUtils.getStackTrace(e)));
                        smsProvider = new DefaultSmsProvider();
                    }
                } else {
                    smsProvider = new DefaultSmsProvider();
                }
            }
        }
        return smsProvider;
    }

    @Override
    public void setSmsProviderClassName(String value) {
        config.setSmsProviderClassName(value);
        smsProvider = null;
    }

    @Override
    public void setUseDefaultSmsSending(boolean value) {
        config.setUseDefaultSmsSending(value);
        if (value) {
            config.setSmsProviderClassName(DefaultSmsProvider.class.getCanonicalName());
            smsProvider = null;
        }
    }

    @Override
    public SendingSms sendSmsSync(SendingSms sendingSms) {
        sendingSms.setErrorCode(0);
        try {
            sendingSms.setSmsId(getSmsProvider().sendSms(sendingSms.getPhone(), sendingSms.getMessage()));
            sendingSms.setStatus(SmsStatus.BUFFERED_SMSC);
        } catch (SmsException e) {
            sendingSms.setErrorCode(e.getCode());
            sendingSms.setStatus(SmsStatus.ERROR);
        }
        sendingSms.setLastChangeDate(timeSource.currentTimestamp());
        if (sendingSms.getAttemptsCount() == null) {
            sendingSms.setAttemptsCount(1);
        } else {
            sendingSms.setAttemptsCount(sendingSms.getAttemptsCount() + 1);
        }
        sendingSms = storeSendingSms(sendingSms);
        return sendingSms;
    }

    @Authenticated
    @Override
    public void scheduledSendSms(SendingSms sendingSms) {
        sendSmsSync(sendingSms);
    }

    @Override
    public SendingSms sendSmsAsync(String phone, String addressee, String message) {
        SendingSms sendingSms = metadata.create(SendingSms.class);
        sendingSms.setPhone(phone);
        sendingSms.setAddressee(addressee);
        sendingSms.setStartSendingDate(timeSource.currentTimestamp());
        sendingSms.setMessage(message);
        sendingSms.setErrorCode(0);
        sendingSms.setAttemptsCount(0);
        sendingSms.setStatus(SmsStatus.IN_QUEUE);
        return sendSmsAsync(sendingSms);
    }

    @Override
    public SendingSms sendSmsAsync(SendingSms sendingSms) {
        return smsManager.addSmsToQueue(Collections.singletonList(sendingSms)).get(0);
    }

    @Override
    public List<SendingSms> sendSmsAsync(List<SendingSms> sendingSmsList) {
        return smsManager.addSmsToQueue(sendingSmsList);
    }

    @Override
    public SendingSms checkStatus(SendingSms sendingSms) {
        sendingSms.setLastChangeDate(timeSource.currentTimestamp());
        try {
            sendingSms.setErrorCode(0);
            sendingSms.setStatus(SmsStatus.fromString(getSmsProvider().checkStatus(sendingSms.getSmsId())));
        } catch (SmsException e) {
            sendingSms.setErrorCode(e.getCode());
            sendingSms.setStatus(SmsStatus.ERROR);
        }
        sendingSms = storeSendingSms(sendingSms);
        return sendingSms;
    }

    @Authenticated
    @Override
    public void scheduledCheckStatusSms(SendingSms sendingSms) {
        checkStatus(sendingSms);
    }

    private SendingSms storeSendingSms(SendingSms sendingSms) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            if (PersistenceHelper.isNew(sendingSms))
                em.persist(sendingSms);
            else
                em.merge(sendingSms);
            tx.commit();
            return sendingSms;
        } catch (Exception e) {
            log.error("Failed to store sms: " + ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            tx.end();
        }
    }
}
