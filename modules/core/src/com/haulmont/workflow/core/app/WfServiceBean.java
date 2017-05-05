/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service(WfService.NAME)
public class WfServiceBean implements WfService {

    @Inject
    protected WfEngineAPI wfEngine;

    @Inject
    protected WfWorkerAPI wfWorkerAPI;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Persistence persistence;

    @Inject
    protected Metadata metadata;

    private Log log = LogFactory.getLog(WfServiceBean.class);

    @Override
    public AssignmentInfo getAssignmentInfo(Card card) {
        return wfWorkerAPI.getAssignmentInfo(card);
    }

    @Override
    public AssignmentInfo getAssignmentInfo(Assignment assignment, String processId) {
        return wfWorkerAPI.getAssignmentInfo(assignment, processId);
    }

    @Override
    public Set<AssignmentInfo> getAssignmentInfos(Card card) {
        return wfWorkerAPI.getAssignmentInfos(card);
    }

    @Override
    public void cancelProcess(Card card) {
        Transaction tx = persistence.createTransaction();
        try {
            wfEngine.cancelProcess(card);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public Card startProcess(Card card) {
        return startProcess(card, null);
    }

    @Override
    public Card startProcess(Card card, Card subCard) {
        Transaction tx = persistence.createTransaction();
        try {
            Card c = wfEngine.startProcess(card, subCard);
            tx.commit();
            return c;
        } finally {
            tx.end();
        }
    }

    @Override
    public void finishAssignment(UUID assignmentId, String outcome, String comment) {
        finishAssignment(assignmentId, outcome, comment, null);
    }

    @Override
    public void finishAssignment(UUID assignmentId, String outcome, String comment, Card subProcCard) {
        WfHelper.getEngine().finishAssignment(assignmentId, outcome, comment, subProcCard);
    }

    @Override
    public Map<String, Object> getProcessVariables(Card card) {
        return wfWorkerAPI.getProcessVariables(reload(card));
    }

    protected Card reload(Card card) {
        Card result = card;
        Transaction transaction = persistence.createTransaction();
        try {
            result = persistence.getEntityManager().find(Card.class, card.getId(), "v-jbpmProcessId");
            transaction.commit();
        } finally {
            transaction.end();
        }
        return result;
    }

    @Override
    public void setProcessVariables(Card card, Map<String, Object> variables) {
        wfWorkerAPI.setProcessVariables(card, variables);
    }

    @Override
    public boolean isCurrentUserInProcRole(Card card, String procRoleCode) {
        return wfWorkerAPI.isCurrentUserInProcRole(card, procRoleCode);
    }

    @Override
    public boolean isUserInProcRole(Card card, User user, String procRoleCode) {
        return wfWorkerAPI.isUserInProcRole(card, user, procRoleCode);
    }

    @Override
    public int deleteNotifications(Card card, User user) {
        return wfWorkerAPI.deleteNotifications(card, user);
    }

    @Override
    public void deleteNotifications(Card card, User user, int type) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select ci from wf$CardInfo ci where ci.card.id = ?1 and ci.user.id = ?2 and " +
                    "ci.type = ?3");
            query.setParameter(1, card.getId());
            query.setParameter(2, user.getId());
            query.setParameter(3, type);
            List<CardInfo> cardInfoList = query.getResultList();
            for (CardInfo ci : cardInfoList)
                em.remove(ci);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void processTimer(TimerEntity timer) {
        TimerManagerAPI timerManager = AppBeans.get(TimerManagerAPI.NAME);
        timerManager.processTimer(timer);
    }

    @Override
    public void deleteNotification(CardInfo cardInfo, User user) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select ci from wf$CardInfo ci where ci.id = ?1 and ci.user.id = ?2");
            query.setParameter(1, cardInfo.getId());
            query.setParameter(2, user.getId());
            List<CardInfo> cardInfoList = query.getResultList();
            for (CardInfo ci : cardInfoList)
                em.remove(ci);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public boolean isCurrentUserContainsRole(Role role) {
        if (role == null)
            return true;
        boolean isRoleContains = false;
        User user = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query q = em.createQuery();

            q.setQueryString("select r from sec$UserRole r where r.user.id=:userId");
            q.setParameter("userId", user.getId());
            List<UserRole> userRoles = q.getResultList();

            for (UserRole userRole : userRoles) {
                if (userRole.getRole().equals(role)) {
                    isRoleContains = true;
                }
            }
            tx.commit();
            return isRoleContains;

        } finally {
            tx.end();
        }
    }

    @Override
    public void setHasAttachmentsInCard(Card card, Boolean hasAttachments) {
        wfWorkerAPI.setHasAttachmentsInCard(card, hasAttachments);
    }

    @Override
    public Card createSubProcCard(Card parentCard, String procCode) {
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Card familyTop = parentCard.getFamilyTop();
            Card card = metadata.create(Card.class);
            card.setProc(getProc(procCode));
            ProcFamily procFamily = metadata.create(ProcFamily.class);
            procFamily.setCard(familyTop);
            procFamily.setJbpmProcessId(familyTop.getJbpmProcessId());
            card.setProcFamily(procFamily);
            CardProc cardProc = metadata.create(CardProc.class);
            cardProc.setCard(card);
            cardProc.setActive(true);
            cardProc.setStartCount(1);
            cardProc.setProc(card.getProc());
            card.setProcs(Arrays.asList(cardProc));
            em.persist(cardProc);
            em.persist(card);
            tx.commit();
            return card;
        } finally {
            tx.end();
        }
    }

    @Override
    public void removeSubProcCard(Card card) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            for (CardProc cardProc : card.getProcs())
                em.remove(cardProc);
            em.remove(card);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public boolean processStarted(Card card) {
        boolean result = false;
        Transaction tx = persistence.getTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            List resultList = em.createQuery("select c.jbpmProcessId from wf$Card c where c.id = :card")
                    .setParameter("card", card)
                    .getResultList();
            if (!resultList.isEmpty())
                result = resultList.get(0) != null;
            tx.commit();
        } finally {
            tx.end();
        }
        return result;
    }

    //TODO: extract logic into separate helper api
    private Proc getProc(String code) {
        EntityManager em = persistence.getEntityManager();
        Query q = em.createQuery("select p from wf$Proc p where p.code = :code").setParameter("code", code);
        List<Proc> result = q.getResultList();
        if (result.isEmpty())
            throw new RuntimeException("Proc not found");
        return result.get(0);
    }

    @Override
    public List<User> getProcessActors(Card card, String procCode, String cardRoleCode) {
        return wfWorkerAPI.getProcessActors(card, procCode, cardRoleCode);
    }
}