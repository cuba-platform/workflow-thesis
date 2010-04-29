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
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jbpm.api.ExecutionService;
import org.jbpm.api.ProcessDefinition;
import org.jbpm.api.ProcessDefinitionQuery;
import org.jbpm.api.ProcessInstance;
import org.jbpm.pvm.internal.client.ClientProcessDefinition;
import org.jbpm.pvm.internal.model.Activity;
import org.jbpm.pvm.internal.model.Transition;
import org.springframework.stereotype.Service;

import java.util.*;

@Service(WfService.NAME)
public class WfServiceBean implements WfService {

    private Log log = LogFactory.getLog(WfServiceBean.class);

    public AssignmentInfo getAssignmentInfo(Card card) {
        AssignmentInfo info = null;
        Transaction tx = Locator.createTransaction();
        try {
            String processId = card.getJbpmProcessId();
            if (processId != null) {
                List<Assignment> assignments = WfHelper.getEngine().getUserAssignments(
                        SecurityProvider.currentOrSubstitutedUserId(), card);
                if (!assignments.isEmpty()) {
                    Assignment assignment = assignments.get(0);
                    info = new AssignmentInfo(assignment);
                    String activityName = assignment.getName();

                    ProcessInstance pi = WfHelper.getExecutionService().findProcessInstanceById(processId);

                    ProcessDefinitionQuery query = WfHelper.getRepositoryService().createProcessDefinitionQuery();
                    ProcessDefinition pd = query.processDefinitionId(pi.getProcessDefinitionId()).uniqueResult();

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
            EntityManager em = PersistenceProvider.getEntityManager();
            card = em.find(Card.class, card.getId());
            if (card.getProc() == null)
                throw new IllegalStateException("Card.proc required");

            ExecutionService es = WfHelper.getExecutionService();
            ProcessInstance pi = es.startProcessInstanceByKey(card.getProc().getJbpmProcessKey(), card.getId().toString());
            card.setJbpmProcessId(pi.getId());

            tx.commit();
            return card;
        } finally {
            tx.end();
        }
    }

    public void finishAssignment(UUID assignmentId, String outcome, String comment) {
        WfHelper.getEngine().finishAssignment(assignmentId, outcome, comment);
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
            EntityManager em = PersistenceProvider.getEntityManager();

            Card c = em.merge(card);

            Query query = em.createQuery("select a from wf$Assignment a where a.card.id = ?1 and a.finished is null");
            query.setParameter("1", c);
            List<Assignment> assignments = query.getResultList();
            for (Assignment assignment : assignments) {
                assignment.setComment(MessageProvider.getMessage(c.getProc().getMessagesPack(), "canceledCard.msg"));
                assignment.setFinished(TimeProvider.currentTimestamp());
            }

            Proc proc = c.getProc();
            for (CardProc cp : c.getProcs()) {
                if (cp.getProc().equals(proc)) {
                    cp.setActive(false);
                }
            }
            c.setJbpmProcessId(null);
            c.setState(WfConstants.CARD_STATE_CANCELED);

            tx.commit();
        } finally {
            tx.end();
        }
    }

    public boolean isCurrentUserInProcRole(Card card, String procRoleCode) {
        User currentUser = SecurityProvider.currentUserSession().getCurrentOrSubstitutedUser();
        return isUserInProcRole(card, currentUser, procRoleCode);
    }
    
    public boolean isUserInProcRole(Card card, User user, String procRoleCode) {
        CardRole appropCardRole = null;
        if (card.getRoles() != null) {
            for (CardRole cardRole : card.getRoles()) {
                if (cardRole.getCode().equals(procRoleCode)) {
                    appropCardRole = cardRole;
                }
            }
            if ((appropCardRole != null) && (appropCardRole.getUser() != null)) {
                return appropCardRole.getUser().equals(user);
            }
        }
        return false;
    }

}
