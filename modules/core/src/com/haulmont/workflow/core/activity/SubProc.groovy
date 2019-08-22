/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.Proc
import org.apache.commons.lang3.StringUtils
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.api.activity.ExternalActivityBehaviour
import org.jbpm.pvm.internal.client.ClientProcessDefinition
import org.jbpm.pvm.internal.env.EnvironmentImpl
import org.jbpm.pvm.internal.model.ExecutionImpl
import org.jbpm.pvm.internal.session.RepositorySession

/**
 *
 *
 */
class SubProc extends CardActivity implements ExternalActivityBehaviour {

    String subProcCode;
    String card;

    @Override
    void execute(ActivityExecution execution) {
        super.execute(execution);
        if (StringUtils.isEmpty(subProcCode))
            throw new RuntimeException("Subprocess code '" + subProcCode + "' is null.");
        RepositorySession repositorySession = EnvironmentImpl.getFromCurrent(RepositorySession.class);
        ClientProcessDefinition processDefinition = repositorySession.findProcessDefinitionByKey(getJbpmKey(subProcCode));
        if (processDefinition == null)
            throw new RuntimeException("Subprocess '" + subProcCode + "' could not be found.");
        ExecutionImpl subProcessInstance = (ExecutionImpl) processDefinition.createProcessInstance(null, execution);
        Card card = findSubProcCard(execution);
        subProcessInstance.setKey(card.getId().toString());
        card.setJbpmProcessId(subProcessInstance.getId());
        card.setState(null);
        if (execution.getVariable("startSubProcessComment") != null) {
            subProcessInstance.createVariable("startProcessComment", (execution.getVariable("startSubProcessComment")));
            execution.removeVariable("startSubProcessComment");
        }
        if (execution.getVariable("subProc_dueDate") != null) {
            subProcessInstance.createVariable("dueDate", (execution.getVariable("subProc_dueDate")));
            execution.removeVariable("subProc_dueDate");
        }
        subProcessInstance.start();
        execution.waitForSignal();
    }


    protected Card findSubProcCard(ActivityExecution execution) {
        if (StringUtils.isEmpty(card))
            throw new RuntimeException("Property card is null.")
        UUID cardId = null;
        try {
            cardId = UUID.fromString(execution.getVariable(card));
            execution.removeVariable(card);
        } catch (Exception e) {
            throw new RuntimeException('Unable to get cardId', e);
        }
        Card card = AppBeans.get(Persistence.class).getEntityManager().find(Card.class, cardId);
        if (card == null)
            throw new RuntimeException("Card not found: $cardId");
        return card;
    }

    protected String getJbpmKey(String code) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Query q = em.createQuery("select p from wf\$Proc p where p.code = :code").setParameter("code", code);
        List<Proc> result = q.getResultList();
        if (result.isEmpty())
            throw new RuntimeException("Subprocess key '" + subProcCode + "' could not be found.");
        return result.get(0).getJbpmProcessKey();
    }

    @Override
    void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
        ExecutionImpl currentExecution = (ExecutionImpl) execution;
        ExecutionImpl subProcessInstance = execution.getSubProcessInstance();
        subProcessInstance.setSuperProcessExecution(null);
        currentExecution.setSubProcessInstance(null);
        //super class executes only after update execution status to prevent errors with db constraints
        super.afterSignal(execution, signalName, parameters);
        execution.takeDefaultTransition();
    }
}
