/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.11.2009 17:04:55
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.security.entity.UserRole;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service(WfService.NAME)
public class WfServiceBean implements WfService {

    @Inject
    private WfEngineAPI wfEngine;

    @Inject
    private UserSessionSource userSessionSource;

    private Log log = LogFactory.getLog(WfServiceBean.class);

    public AssignmentInfo getAssignmentInfo(Card card) {
        AssignmentInfo info = null;
        Transaction tx = Locator.createTransaction();
        try {
            String processId = card.getJbpmProcessId();
            if (processId != null) {
                List<Assignment> assignments = WfHelper.getEngine().getUserAssignments(
                        userSessionSource.currentOrSubstitutedUserId(), card);
                if (!assignments.isEmpty()) {
                    Assignment assignment = assignments.get(0);
                    info = new AssignmentInfo(assignment);
                    String activityName = assignment.getName();
                    if (!card.equals(assignment.getCard()))
                        processId = assignment.getCard().getJbpmProcessId();

                    ProcessInstance pi = WfHelper.getExecutionService().findProcessInstanceById(processId);

                    ProcessDefinitionQuery query = WfHelper.getRepositoryService().createProcessDefinitionQuery();

                    // Getting List instead of uniqueResult because of rare bug in process deployment which leads
                    // to creation of 2 PD with the same ID
                    List<ProcessDefinition> pdList = query.processDefinitionId(pi.getProcessDefinitionId()).list();
                    if (pdList.isEmpty())
                        throw new RuntimeException("ProcessDefinition not found: " + pi.getProcessDefinitionId());
                    Collections.sort(
                            pdList,
                            new Comparator<ProcessDefinition>() {
                                public int compare(ProcessDefinition pd1, ProcessDefinition pd2) {
                                    return pd1.getDeploymentId().compareTo(pd2.getDeploymentId());
                                }
                            }
                    );
                    ProcessDefinition pd = pdList.get(pdList.size() - 1);

                    Activity activity = ((ClientProcessDefinition) pd).findActivity(activityName);
                    for (Transition transition : activity.getOutgoingTransitions()) {
                        info.getActions().add(activityName + "." + transition.getName());
                    }
                }
            }
            tx.commit();
        } finally {
            tx.end();
        }
        return info;
    }

    public Card startProcess(Card card) {
        Transaction tx = Locator.createTransaction();
        try {
            Card c = WfHelper.getEngine().startProcess(card);
            tx.commit();
            return c;
        } finally {
            tx.end();
        }
    }

    public void finishAssignment(UUID assignmentId, String outcome, String comment) {
        finishAssignment(assignmentId, outcome, comment, null);
    }

    public void finishAssignment(UUID assignmentId, String outcome, String comment, Card subProcCard) {
        WfHelper.getEngine().finishAssignment(assignmentId, outcome, comment, subProcCard);
    }

    public Map<String, Object> getProcessVariables(Card card) {
        Map<String, Object> variables = new HashMap<String, Object>();

        Transaction tx = Locator.createTransaction();
        try {
            Set<String> names = WfHelper.getExecutionService().getVariableNames(card.getJbpmProcessId());
            for (String name : names) {
                variables.put(name, WfHelper.getExecutionService().getVariable(card.getJbpmProcessId(), name));
            }
            tx.commit();
            return variables;
        } finally {
            tx.end();
        }
    }

    public void setProcessVariables(Card card, Map<String, Object> variables) {
        Transaction tx = Locator.createTransaction();
        try {
            WfHelper.getExecutionService().setVariables(card.getJbpmProcessId(), variables);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void cancelProcess(Card card) {
        Transaction tx = Locator.createTransaction();
        try {
            wfEngine.cancelProcess(card);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public boolean isCurrentUserInProcRole(Card card, String procRoleCode) {
        User currentUser = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
        return isUserInProcRole(card, currentUser, procRoleCode);
    }

    public boolean isUserInProcRole(Card card, User user, String procRoleCode) {
        CardRole appropCardRole = null;
        if (card.getRoles() == null) {
            Transaction tx = Locator.createTransaction();
            try {
                EntityManager em = PersistenceProvider.getEntityManager();
                em.setView(MetadataProvider.getViewRepository().getView(Card.class, "with-roles"));
                card = em.find(Card.class, card.getId());
                tx.commit();
            } finally {
                tx.end();
            }
        }
        if (card.getRoles() != null) {
            for (CardRole cardRole : card.getRoles()) {
                if (cardRole.getCode().equals(procRoleCode) && cardRole.getUser() != null && cardRole.getUser().equals(user)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteNotifications(Card card, User user) {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("update wf$CardInfo ci set ci.deleteTs = ?1, ci.deletedBy = ?2 " +
                    "where ci.card.id = ?3 and ci.user.id = ?4");
            query.setParameter(1, TimeProvider.currentTimestamp());
            query.setParameter(2, user.getLogin());
            query.setParameter(3, card.getId());
            query.setParameter(4, user.getId());
            query.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void deleteNotifications(Card card, User user, int type) {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("update wf$CardInfo ci set ci.deleteTs = ?1, ci.deletedBy = ?2 " +
                    "where ci.card.id = ?3 and ci.user.id = ?4 and ci.type = ?5");
            query.setParameter(1, TimeProvider.currentTimestamp());
            query.setParameter(2, user.getLogin());
            query.setParameter(3, card.getId());
            query.setParameter(4, user.getId());
            query.setParameter(5, type);
            query.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public void deleteNotification(CardInfo cardInfo, User user) {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("update wf$CardInfo ci set ci.deleteTs = ?1, ci.deletedBy = ?2 " +
                    "where ci.id = ?3 and ci.user.id = ?4");
            query.setParameter(1, TimeProvider.currentTimestamp());
            query.setParameter(2, user.getLogin());
            query.setParameter(3, cardInfo.getId());
            query.setParameter(4, user.getId());
            query.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public boolean isCurrentUserContainsRole(Role role) {
        if (role == null)
            return true;
        boolean isRoleContains = false;
        User user = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
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

    public void setHasAttachmentsInCard(Card card, Boolean hasAttachments) {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("update wf$Card c set c.hasAttachments = ?1 " +
                    "where c.id = ?2");
            query.setParameter(1, hasAttachments);
            query.setParameter(2, card);
            query.executeUpdate();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public Card createSubProcCard(Card parentCard, String procCode) {
        Transaction tx = PersistenceProvider.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Card familyTop = parentCard.getFamilyTop();
            Card card = new Card();
            card.setProc(getProc(procCode));
            ProcFamily procFamily = new ProcFamily();
            procFamily.setCard(familyTop);
            procFamily.setJbpmProcessId(familyTop.getJbpmProcessId());
            card.setProcFamily(procFamily);
            CardProc cardProc = new CardProc();
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

    public void removeSubProcCard(Card card) {
        Transaction tx = PersistenceProvider.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            for (CardProc cardProc : card.getProcs())
                em.remove(cardProc);
            em.remove(card);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    //TODO: extract logic into separate helper api
    private Proc getProc(String code) {
        EntityManager em = PersistenceProvider.getEntityManager();
        Query q = em.createQuery("select p from wf$Proc p where p.code = :code").setParameter("code", code);
        List<Proc> result = q.getResultList();
        if (result.isEmpty())
            throw new RuntimeException("Proc not found");
        return result.get(0);
    }
}