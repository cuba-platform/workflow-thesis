/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.*;

/**
 * {@link WfService} delegate in middle ware.
 *
 * @author Sergey Saiyan
 * @version $Id$
 */
@ManagedBean(WfWorkerAPI.NAME)
public class WfWorkerBean implements WfWorkerAPI {

    @Inject
    protected Persistence persistence;

    @Inject
    protected UserSessionSource userSessionSource;

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
                    public int compare(ProcessDefinition pd1, ProcessDefinition pd2) {
                        return pd1.getDeploymentId().compareTo(pd2.getDeploymentId());
                    }
                }
        );
        ProcessDefinition pd = pdList.get(pdList.size() - 1);

        Activity activity = ((ClientProcessDefinition) pd).findActivity(activityName);
        addActionsToAssignmentInfo(info, activityName, activity, card, assignment);
        return info;
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
}
