/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.app.Authentication;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author novikov
 * @version $Id$
 */
@ManagedBean(SmsManagerAPI.NAME)
public class SmsManager implements SmsManagerAPI {

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

    @Inject
    protected Metadata metadata;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Authentication authentication;

    private SmsSenderConfig config;

    @Inject
    public void setConfiguration(Configuration configuration) {
        this.config = configuration.getConfig(SmsSenderConfig.class);
    }

    @Override
    public List<SendingSms> addSmsToQueue(List<SendingSms> sendingSmsList) {
        if (!config.getUseSmsSending())
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
            if (!config.getUseSmsSending())
                return;
            int delay = config.getDelayCallCount();
            if (callCount >= delay) {
                log.debug("Queueing Smses");
                authentication.begin();
                try {
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
                    updateStatusForNotSetSms();
                } finally {
                    authentication.end();
                }
            } else {
                callCount++;
            }
        } catch (Throwable e) {
            log.error("Exception in queue sms to send:" + e);
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
            EntityManager em = persistence.getEntityManager();
            TypedQuery<SendingSms> query = em.createQuery("select sms from wf$SendingSms sms where " +
                    "sms.status in (0, 400, 500) and sms.attemptsCount < :attemptsCount and sms.errorCode = 0 and " +
                    "sms.startSendingDate > :startDate and sms.startSendingDate <=:currentDay order by sms.createTs",
                    SendingSms.class);
            query.setParameter("attemptsCount", config.getDefaultSendingAttemptsCount())
                    .setParameter("startDate", DateUtils.addSeconds(timeSource.currentTimestamp(),
                            -config.getMaxSendingTimeSec()))
                    .setParameter("currentDay", timeSource.currentTimestamp());
            query.setViewName("_local");
            List<SendingSms> res = query.setMaxResults(config.getMessageQueueCapacity()).getResultList();
            tx.commit();
            return res;
        } finally {
            tx.end();
        }
    }

    private List<SendingSms> loadSmsNotSent() {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            TypedQuery<SendingSms> query = em.createQuery("select sms from wf$SendingSms sms where " +
                    "sms.status in (0) and (sms.attemptsCount > :attemptsCount or sms.startSendingDate <= :startDate) and " +
                    "sms.errorCode = 0 order by sms.createTs", SendingSms.class);
            query.setParameter("attemptsCount", config.getDefaultSendingAttemptsCount())
                    .setParameter("startDate", DateUtils.addSeconds(timeSource.currentTimestamp(),
                            -config.getMaxSendingTimeSec()));
            query.setViewName("_local");
            List<SendingSms> res = query.setMaxResults(config.getMessageQueueCapacity()).getResultList();
            tx.commit();
            return res;
        } finally {
            tx.end();
        }
    }

    private void updateStatusForNotSetSms() {
        List<SendingSms> notSentMessages = loadSmsNotSent();
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            for (SendingSms msg : notSentMessages) {
                msg.setStatus(SmsStatus.NON_DELIVERED);
                em.merge(msg);
            }
            tx.commit();
        } finally {
            tx.end();
        }
    }
}
