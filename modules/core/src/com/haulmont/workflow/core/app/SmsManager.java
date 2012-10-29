/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ManagementBean;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.global.LoginException;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
@ManagedBean(SmsManagerAPI.NAME)
public class SmsManager extends ManagementBean implements SmsManagerMBean, SmsManagerAPI {

    private static final Log log = LogFactory.getLog(SmsManager.class);

    private Set<SendingSms> messageQueue;
    private static int callCount = 0;
    private static final int MAX_THREADS = 5;

    private ExecutorService smsSenderTaskExecutor = Executors.newFixedThreadPool(MAX_THREADS, new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r, "SmsSendTaskThread");
            thread.setDaemon(true);
            return thread;
        }
    });


    @Inject
    private UserSessionSource userSessionSource;

    @Inject
    private Persistence persistence;

    private SmsSenderConfig config;

    @Inject
    public void setConfig(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    @Override
    public String getDelayCallCountJmx() {
        return String.valueOf(config.getDelayCallCount());
    }

    @Override
    public String getMessageQueueCapacityJmx() {
        return String.valueOf(config.getMessageQueueCapacity());
    }

    @Override
    public List<SendingSms> addSmsToQueue(List<SendingSms> sendingSmsList) {
        if (!getUseSmsSending())
            return sendingSmsList;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            for (SendingSms message : sendingSmsList) {
                em.persist(message);
            }
            tx.commit();
        } finally {
            tx.end();
        }
        return sendingSmsList;
    }

    @Override
    public void queueSmsToSend() {
        try {
            if (!getUseSmsSending())
                return;
            int delay = config.getDelayCallCount();
            if (callCount >= delay) {
                log.debug("Queueing Smses");
                loginOnce();
                List<SendingSms> loadedMessages = loadSmsToSend();
                if (messageQueue == null)
                    messageQueue = new LinkedHashSet<SendingSms>();
                messageQueue.addAll(loadedMessages);

                List<SendingSms> processedMessages = new ArrayList<SendingSms>();
                List<UUID> notSentMessageIds = new ArrayList<UUID>();
                for (SendingSms msg : messageQueue) {
                    if (SmsStatus.IN_QUEUE.equals(msg.getStatus()))
                        sendAsync(msg);
                    else
                        checkStatusAsync(msg);
                    processedMessages.add(msg);
                }
                messageQueue.removeAll(processedMessages);
            } else {
                callCount++;
            }
        } catch (Throwable e) {
            log.error("Exception in queue sms to send:" + e);
        }
    }

    @Override
    public boolean getUseSmsSending() {
        return config.getUseSmsSending();
    }

    @Override
    public void setUseSmsSending(boolean value) {
        try {
            login();
            config.setUseSmsSending(value);
        } catch (LoginException e) {
            throw new RuntimeException(e);
        } finally {
            logout();
        }
    }

    private void sendAsync(SendingSms msg) {
        try {
            Runnable smsSendTask = new SmsSendTask(msg);
            smsSenderTaskExecutor.execute(smsSendTask);
        } catch (Exception e) {
            log.error("Exception while sending sms: " + ExceptionUtils.getStackTrace(e));
        }
    }

    private void checkStatusAsync(SendingSms msg) {
        try {
            Runnable smsCheckStatusTask = new SmsCheckStatusTask(msg);
            smsSenderTaskExecutor.execute(smsCheckStatusTask);
        } catch (Exception e) {
            log.error("Exception while sending sms: " + ExceptionUtils.getStackTrace(e));
        }
    }

    private List<SendingSms> loadSmsToSend() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            em.setView(MetadataProvider.getViewRepository().getView(SendingSms.class, "_local"));
            Query query = em.createQuery("select sms from wf$SendingSms sms where sms.status in (0, 400, 500) and " +
                    "sms.attemptsCount < :attemptsCount and sms.errorCode = 0 order by sms.createTs")
                    .setParameter("attemptsCount", config.getDefaultSendingAttemptsCount());
            List<SendingSms> res = query.setMaxResults(config.getMessageQueueCapacity()).getResultList();
            tx.commit();
            return res;
        } finally {
            tx.end();
        }
    }
}