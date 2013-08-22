package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
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
}
