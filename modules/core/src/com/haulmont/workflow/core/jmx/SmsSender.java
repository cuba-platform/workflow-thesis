/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.jmx;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.app.SmsSenderAPI;
import com.haulmont.workflow.core.app.SmsSenderConfig;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;
import com.haulmont.workflow.core.exception.SmsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.util.List;

@Component("workflow_SmsSenderMBean")
public class SmsSender implements SmsSenderMBean {

    protected Log log = LogFactory.getLog(getClass());

    @Inject
    protected SmsSenderAPI sender;

    @Inject
    protected Messages messages;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    @Inject
    protected TimeSource timeSource;

    protected SmsSenderConfig config;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    @Authenticated
    @Override
    public String sendSms(String phone, String addressee, String message) {
        SendingSms sendingSms = metadata.create(SendingSms.class);
        sendingSms.setPhone(phone);
        sendingSms.setAddressee(addressee);
        sendingSms.setStartSendingDate(timeSource.currentTimestamp());
        sendingSms.setMessage(message);
        sendingSms.setErrorCode(0);
        sendingSms.setAttemptsCount(0);
        sendingSms.setStatus(SmsStatus.IN_QUEUE);
        sendingSms = sender.sendSmsSync(sendingSms);
        return sendingSms.getErrorCode() == 0 ? sendingSms.getSmsId() :
                messages.getMessage(SmsException.class, "smsException." + sendingSms.getErrorCode());
    }


    @Authenticated
    @Override
    public String checkStatus(String smsId) {
        if (StringUtils.isBlank(smsId))
            return "smsId is empty";

        Transaction tx = persistence.createTransaction();
        SendingSms sendingSms = null;
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select sms from wf$SendingSms sms where sms.smsId = :smsId");
            query.setParameter("smsId", smsId);
            List list = query.getResultList();
            sendingSms = list.isEmpty() ? null : (SendingSms) list.get(0);
            tx.commit();
        } catch (Exception e) {
            log.error("Failed to store sms: " + ExceptionUtils.getStackTrace(e));
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
        sendingSms = sender.checkStatus(sendingSms);
        return sendingSms.getErrorCode() == 0 ? messages.getMessage(sendingSms.getStatus()) :
                messages.getMessage(SmsException.class, "smsException." + sendingSms.getErrorCode());
    }

    @Authenticated
    @Override
    public String getBalance() {
        try {
            return sender.getSmsProvider().getBalance();
        } catch (SmsException e) {
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public boolean getUseDefaultSmsSending() {
        return config.getUseDefaultSmsSending();
    }

    @Authenticated
    @Override
    public void setUseDefaultSmsSending(boolean value) {
        sender.setUseDefaultSmsSending(value);
    }

    @Override
    public String getSmsProviderClassName() {
        return config.getSmsProviderClassName();
    }

    @Authenticated
    @Override
    public void setSmsProviderClassName(String value) {
        sender.setSmsProviderClassName(value);
    }

    @Override
    public int getSmsMaxParts() {
        return config.getSmsMaxParts();
    }

    @Override
    public void setSmsMaxParts(int value) {
        config.setSmsMaxParts(value);
    }
}
