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
import com.haulmont.cuba.core.global.MetadataProvider;
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

import javax.inject.Inject;
import java.util.*;

@Service(WfService.NAME)
public class WfServiceBean implements WfService {

    @Inject
    private WfEngineAPI wfEngine;

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
            Card c = WfHelper.getEngine().startProcess(card);
            tx.commit();
            return c;
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
            wfEngine.cancelProcess(card);
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
}
