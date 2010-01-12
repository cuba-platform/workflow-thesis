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
import com.haulmont.workflow.core.WfHelper;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
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

import java.util.List;
import java.util.UUID;

@Service(WfService.NAME)
public class WfServiceBean implements WfService {

    private Log log = LogFactory.getLog(WfServiceBean.class);

    public AssignmentInfo getAssignmentInfo(Card card) {
        AssignmentInfo info = null;
        Transaction tx = Locator.createTransaction();
        try {
            String processId = card.getJbpmProcessId();
            if (processId != null) {
                List<Assignment> assignments = WfHelper.getWfEngineAPI().getUserAssignments(
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
        WfHelper.getWfEngineAPI().finishAssignment(assignmentId, outcome, comment);
    }
}
