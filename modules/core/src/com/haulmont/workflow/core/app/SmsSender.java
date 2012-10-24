/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ManagementBean;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;
import com.haulmont.workflow.core.exception.SmsException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
@ManagedBean(SmsSenderAPI.NAME)
public class SmsSender extends ManagementBean implements SmsSenderAPI, SmsSenderMBean {

    private static final Log log = LogFactory.getLog(SmsSender.class);

    @Inject
    private SmsManagerAPI smsManager;

    @Inject
    private Persistence persistence;

    private SmsSenderConfig config;

    private SmsProvider smsProvider;

    @Inject
    public void setConfig(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    private SmsProvider getSmsProvider() {
        if (getUseDefaultSmsSending()) {
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
    public String sendSms(String phone, String message) {
        try {
            login();
            SendingSms sendingSms = MetadataProvider.create(SendingSms.class);
            sendingSms.setPhone(phone);
            sendingSms.setMessage(message);
            sendingSms.setErrorCode(0);
            sendingSms.setAttemptsCount(0);
            sendingSms.setStatus(SmsStatus.IN_QUEUE);
            sendingSms = sendSmsSync(sendingSms);
            return sendingSms.getErrorCode() == 0 ? sendingSms.getSmsId() :
                    MessageProvider.getMessage(SmsException.class, "smsException." + sendingSms.getErrorCode());
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    @Override
    public String checkStatus(String smsId) {
        try {
            login();
            Transaction tx = persistence.createTransaction();
            SendingSms sendingSms = null;
            try {
                EntityManager em = PersistenceProvider.getEntityManager();
                Query query = em.createQuery("select sms from wf$SendingSms sms where sms.smsId = :smsId");
                query.setParameter("smsId", smsId);
                List list = query.getResultList();
                sendingSms = list.isEmpty() ? null : (SendingSms) list.get(0);
                tx.commit();
            } catch (Exception e) {
                log.error("Failed to store sms: " + ExceptionUtils.getStackTrace(e));
                return null;
            } finally {
                tx.end();
            }
            sendingSms = checkStatus(sendingSms);
            return sendingSms.getErrorCode() == 0 ? MessageProvider.getMessage(sendingSms.getStatus()) :
                    MessageProvider.getMessage(SmsException.class, "smsException." + sendingSms.getErrorCode());
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    @Override
    public String getBalance() throws SmsException {
        try {
            login();
            return getSmsProvider().getBalance();
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    @Override
    public boolean getUseDefaultSmsSending() {
        return config.getUseDefaultSmsSending();
    }

    @Override
    public void setUseDefaultSmsSending(boolean value) {
        try {
            login();
            config.setUseDefaultSmsSending(value);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    @Override
    public String getSmsProviderClassName() {
        return config.getSmsProviderClassName();
    }

    @Override
    public void setSmsProviderClassName(String value) {
        try {
            login();
            config.setSmsProviderClassName(value);
            smsProvider = null;
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
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
        sendingSms.setLastChangeDate(TimeProvider.currentTimestamp());
        if (sendingSms.getAttemptsCount() == null) {
            sendingSms.setAttemptsCount(1);
        } else {
            sendingSms.setAttemptsCount(sendingSms.getAttemptsCount() + 1);
        }
        sendingSms = storeSendingSms(sendingSms);
        return sendingSms;
    }

    @Override
    public void scheduledSendSms(SendingSms sendingSms) throws LoginException {
        loginOnce();
        sendSmsSync(sendingSms);
    }

    @Override
    public SendingSms sendSmsAsync(String phone, String message) {
        SendingSms sendingSms = MetadataProvider.create(SendingSms.class);
        sendingSms.setPhone(phone);
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
        sendingSms.setLastChangeDate(TimeProvider.currentTimestamp());
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

    @Override
    public void scheduledCheckStatusSms(SendingSms sendingSms) throws LoginException {
        loginOnce();
        checkStatus(sendingSms);
    }

    private SendingSms storeSendingSms(SendingSms sendingSms) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
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
