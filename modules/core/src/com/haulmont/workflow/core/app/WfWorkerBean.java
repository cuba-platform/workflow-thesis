/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardInfo;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.global.AssignmentInfo;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.api.model.Activity;
import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.*;

/**
 * {@link WfService} delegate in middle ware.
 */
@Component(WfWorkerAPI.NAME)
public class WfWorkerBean implements WfWorkerAPI {

    @Inject
    protected Persistence persistence;

    @Inject
    protected UserSessionSource userSessionSource;

    private Log log = LogFactory.getLog(WfWorkerBean.class);

    @Override
    public Set<AssignmentInfo> getAssignmentInfos(Card card, UUID userId) {
        Set<AssignmentInfo> infos = new LinkedHashSet<>();
        Transaction tx = persistence.createTransaction();
        try {
            String processId = card.getJbpmProcessId();
            if (processId != null) {
                List<Assignment> assignments = WfHelper.getEngine().getUserAssignments(userId, card);
                if (!assignments.isEmpty()) {
                    for (Assignment assignment : assignments) {
                        if (card.equals(assignment.getCard()) && !processId.equals(assignment.getCard().getJbpmProcessId())) {
                            return infos;
                        }
                        if (!card.equals(assignment.getCard()))
                            processId = assignment.getCard().getJbpmProcessId();
                        AssignmentInfo info = getAssignmentInfo(assignment, processId, card);
                        infos.add(info);
                    }
                }
            }
            tx.commit();
        } finally {
            tx.end();
        }
        return infos;
    }

    @Override
    public Set<AssignmentInfo> getAssignmentInfos(Card card) {
        return getAssignmentInfos(card, userSessionSource.currentOrSubstitutedUserId());
    }

    @Override
    public AssignmentInfo getAssignmentInfo(Card card) {
        AssignmentInfo info = null;
        Transaction tx = persistence.createTransaction();
        try {
            String processId = card.getJbpmProcessId();
            if (processId != null) {
                List<Assignment> assignments = WfHelper.getEngine().getUserAssignments(
                        userSessionSource.currentOrSubstitutedUserId(), card);
                if (!assignments.isEmpty()) {
                    Assignment assignment = assignments.get(0);
                    if (card.equals(assignment.getCard()) && !processId.equals(assignment.getCard().getJbpmProcessId())) {
                        return null;
                    }
                    if (!card.equals(assignment.getCard()))
                        processId = assignment.getCard().getJbpmProcessId();
                    info = getAssignmentInfo(assignment, processId, card);
                }
            }
            tx.commit();
        } finally {
            tx.end();
        }
        return info;
    }

    @Override
    public AssignmentInfo getAssignmentInfo(Assignment assignment, String processId) {
        return getAssignmentInfo(assignment, processId, null);
    }

    public AssignmentInfo getAssignmentInfo(Assignment assignment, String processId, Card card) {
        Transaction tx = persistence.getTransaction();
        try {
            AssignmentInfo info = new AssignmentInfo(assignment);
            String activityName = assignment.getName();
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
                        @Override
                        public int compare(ProcessDefinition pd1, ProcessDefinition pd2) {
                            return pd1.getDeploymentId().compareTo(pd2.getDeploymentId());
                        }
                    }
            );

            ProcessDefinition pd = pdList.get(pdList.size() - 1);
            Activity activity = ((ClientProcessDefinition) pd).findActivity(activityName);
            addActionsToAssignmentInfo(info, activityName, activity, card, assignment);
            tx.commit();
            return info;
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
            return null;
        } finally {
            tx.end();
        }
    }

    @SuppressWarnings("unused")
    protected void addActionsToAssignmentInfo(AssignmentInfo info, String activityName, Activity activity,
                                              Card card, Assignment assignment) {
        for (Transition transition : activity.getOutgoingTransitions()) {
            info.getActions().add(activityName + "." + transition.getName());
        }
    }

    @Override
    public Map<String, Object> getProcessVariables(Card card) {
        Map<String, Object> variables = new HashMap<String, Object>();

        Transaction tx = persistence.createTransaction();
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

    @Override
    public void setProcessVariables(Card card, Map<String, Object> variables) {
        Transaction tx = persistence.createTransaction();
        try {
            WfHelper.getExecutionService().setVariables(card.getJbpmProcessId(), variables);
            tx.commit();
        } finally {
            tx.end();
        }
    }

    @Override
    public void setHasAttachmentsInCard(Card card, Boolean hasAttachments) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
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

    @Override
    public List<User> getProcessActors(Card card, String procCode, String cardRoleCode) {
        List<User> result = new ArrayList<>();
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            result = em.createQuery("select cr.user from wf$CardRole cr where cr.card.id = :card " +
                    "and cr.procRole.proc.code = :procCode " +
                    "and cr.code = :cardRoleCode")
                    .setParameter("card", card)
                    .setParameter("procCode", procCode)
                    .setParameter("cardRoleCode", cardRoleCode)
                    .getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
        return result;
    }

    @Override
    public boolean isCurrentUserInProcRole(Card card, String procRoleCode) {
        User currentUser = userSessionSource.getUserSession().getCurrentOrSubstitutedUser();
        return isUserInProcRole(card, currentUser, procRoleCode);
    }

    @Override
    public boolean isUserInProcRole(Card card, User user, String procRoleCode) {
        CardRole appropCardRole = null;
        if (card.getRoles() == null) {
            Transaction tx = persistence.createTransaction();
            try {
                EntityManager em = persistence.getEntityManager();
                card = em.find(Card.class, card.getId(), "with-roles");
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

    @Override
    public int deleteNotifications(Card card, User user) {
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            Query query = em.createQuery("select ci from wf$CardInfo ci where ci.card.id = ?1 and ci.user.id = ?2");
            query.setParameter(1, card.getId());
            query.setParameter(2, user.getId());
            List<CardInfo> cardInfoList = query.getResultList();
            for (CardInfo ci : cardInfoList)
                em.remove(ci);
            tx.commit();
            return cardInfoList.size();
        } finally {
            tx.end();
        }
    }
}
