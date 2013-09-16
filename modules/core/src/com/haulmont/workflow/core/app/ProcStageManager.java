/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.ClusterManagerAPI;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.security.app.Authentication;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.*;
import groovy.lang.Binding;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.*;

@ManagedBean(ProcStageManagerAPI.NAME)
public class ProcStageManager implements ProcStageManagerAPI {
    private Log log = LogFactory.getLog(ProcStageManager.class);

    @Inject
    private ClusterManagerAPI clusterManager;

    @Inject
    protected WfMailWorker wfMailWorker;

    @Inject
    protected Persistence persistence;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Scripting scripting;

    @Inject
    protected Authentication authentication;

    @Override
    public void processOverdueStages() {
        if (!AppContext.isStarted() || !clusterManager.isMaster())
            return;

        log.info("Notifying about overdue stages");
        authentication.begin();
        try {
            Transaction tx = persistence.getTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                Query query = em.createQuery("select cs from wf$CardStage cs left join cs.procStage ps left join fetch ps.procRoles where cs.endDateFact is null " +
                        "and cs.endDatePlan < :currentTime and cs.notified <> true");
                Date currentTime = timeSource.currentTimestamp();
                query.setParameter("currentTime", currentTime);

                List<CardStage> list = query.getResultList();
                if (list != null) {
                    for (CardStage cardStage : list) {
                        Set<User> addressees = new HashSet<User>();
                        List<ProcRole> procRoles = cardStage.getProcStage().getProcRoles();
                        if (!CollectionUtils.isEmpty(procRoles)) {
                            for (ProcRole procRole : procRoles) {
                                List<User> users = getUsersInProcRole(cardStage.getCard(), procRole);
                                addressees.addAll(users);
                            }
                        }
                        if (cardStage.getProcStage().getNotifyCurrentActor()) {
                            Query assignmentQuery = em.createQuery("select u from wf$Assignment a join a.user u where a.card.id = :card and a.finished is null");
                            assignmentQuery.setParameter("card", cardStage.getCard());
                            List<User> assignmentUsers = assignmentQuery.getResultList();
                            addressees.addAll(assignmentUsers);
                        }
                        for (User user : addressees) {
                            createNotifications(cardStage, user);
                        }

                        cardStage.setNotified(true);
                    }
                }
                tx.commit();
            } finally {
                tx.end();
            }
        } finally {
            authentication.end();
        }
    }

    private void createNotifications(CardStage cardStage, final User user) {
        String scriptName = cardStage.getProcStage().getNotificationScript();

        String script = cardStage.getCard().getProc().getMessagesPack().replaceAll("\\.", "/")
                + "/" + (StringUtils.isEmpty(scriptName) ? "OverdueStageNotification.groovy" : scriptName);
        String subject;
        String body;
        try {
            Map<String, Object> bindingParams = new HashMap<String, Object>();
            bindingParams.put("card", cardStage.getCard());
            bindingParams.put("cardStage", cardStage);
            bindingParams.put("user", user);

            Binding binding = new Binding(bindingParams);
            scripting.runGroovyScript(script, binding);
            subject = binding.getVariable("subject").toString();
            body = binding.getVariable("body").toString();
        } catch (Exception e) {
            log.error("Unable eveluate groovy script " + script, e);
            Proc proc = cardStage.getCard().getProc();
            String procStr = proc == null ? null : " of process " + proc.getName();
            subject = "Stage " + cardStage.getProcStage().getName() + procStr + " is overdue";
            body = "Stage " + cardStage.getProcStage().getName() + procStr + " is overdue";
        }

        createCardInfo(cardStage.getCard(), user, subject);

        final String emailSubject = subject;
        final String emailBody = body;
        new Thread() {
            @Override
            public void run() {
                try {
                    wfMailWorker.sendEmail(user, emailSubject, emailBody);
                } catch (Exception e) {
                    log.error("Unable to send email to " + user.getEmail(), e);
                }
            }
        }.start();
    }

    protected CardInfo createCardInfo(Card card, User user, String subject) {
        CardInfo ci = new CardInfo();
        ci.setCard(card);
        ci.setType(CardInfo.TYPE_OVERDUE);
        ci.setUser(user);
        ci.setJbpmExecutionId(card.getJbpmProcessId());
//        ci.setActivity(context.getActivity());
        ci.setDescription(subject);

        EntityManager em = persistence.getEntityManager();
        em.persist(ci);

        return ci;
    }

    private List<User> getUsersInProcRole(Card card, ProcRole procRole) {
        List<User> list = new ArrayList<User>();
        if (card.getRoles() != null) {
            for (CardRole cardRole : card.getRoles()) {
                if (cardRole.getProcRole().equals(procRole) && (cardRole.getUser() != null))
                    list.add(cardRole.getUser());
            }
        }
        return list;
    }
}
