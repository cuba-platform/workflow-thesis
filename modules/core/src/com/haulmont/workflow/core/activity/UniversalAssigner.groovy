/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */



package com.haulmont.workflow.core.activity

import org.jbpm.api.activity.ActivityExecution
import com.google.common.base.Preconditions
import org.apache.commons.lang.StringUtils
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.cuba.core.global.TimeProvider
import com.haulmont.cuba.core.Locator
import com.haulmont.cuba.core.Query
import org.apache.commons.logging.LogFactory
import org.apache.commons.logging.Log
import com.haulmont.workflow.core.WfHelper
import org.jbpm.api.ExecutionService

/**
 *
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class UniversalAssigner extends MultiAssigner {

    private Log log = LogFactory.getLog(Assigner.class)

    //todo add stage duration control!
    @Override
    protected boolean createAssignment(ActivityExecution execution) {

        EntityManager em = PersistenceProvider.getEntityManager()

        Card card = findCard(execution)

        List<CardRole> srcCardRoles = getCardRoles(execution, card)
        def cardRoles = []
        if (srcCardRoles) {
            int minSortOrder = srcCardRoles[0].sortOrder;
            cardRoles = srcCardRoles.findAll {CardRole cr -> cr.sortOrder == minSortOrder}
        }

        if (cardRoles.isEmpty()) {
            if (forRefusedOnly(execution)) {
                log.debug("No users to assign: cardId=${card.getId()}, procRole=$role")
                return false
            } else {
                def pr = getProcRoleByCode(card, role)
                throw new WorkflowException(WorkflowException.Type.NO_CARD_ROLE,
                        "User not found: cardId=${card.getId()}, procRole=$role", pr?.name ? pr.name : role)
            }
        }

        if (srcCardRoles.size() == 1) {
            createUserAssignment(execution, card, cardRoles[0], null)
        } else {
            Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

            Assignment master = new Assignment()
            master.setName(execution.getActivityName())
            master.setJbpmProcessId(execution.getProcessInstance().getId())
            master.setCard(card)
            em.persist(master)

            cardRoles.each {CardRole cr -> createUserAssignment(execution, card, cr, master)}
        }

        return true
    }

    @Override
    void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
        if (parameters == null)
            throw new RuntimeException('Assignment object expected')
        Preconditions.checkState(Locator.isInTransaction(), 'An active transaction required')

        Assignment assignment = (Assignment) parameters.get("assignment")

        if (assignment.getMasterAssignment() == null) {
            log.debug("No master assignment, just taking $signalName")
            assignment.setFinished(TimeProvider.currentTimestamp());
            execution.take(signalName)
            if (timersFactory) {
                timersFactory.removeTimers(execution)
            }
            onSuccess(execution, signalName, assignment)
            afterSignal(execution, signalName, parameters)
        } else {
            if (timersFactory) {
                timersFactory.removeTimers(execution, assignment)
            }
            //todo change log message
            log.debug("Trying to finish assignment")

            onSuccess(execution, signalName, assignment)

            String resultTransition = signalName

            def siblings = getSiblings(assignment)
            for (Assignment sibling: siblings) {
                if (!sibling.finished) {
                    log.debug("Parallel assignment is not finished: assignment.id=${sibling.id}")
                    execution.waitForSignal()
                    return
                }

                if (sibling.getOutcome() != successTransition)
                    resultTransition = sibling.getOutcome()
            }


            ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
            Map<String, Object> params = new HashMap<String, Object>()
            params.put("assignment", assignment.getMasterAssignment())

            if (resultTransition != successTransition) {
                log.debug("Non-success transition has taken, signal master")

                es.signalExecutionById(execution.getId(), resultTransition, params)
                afterSignal(execution, signalName, parameters)
            } else {
                def cardRoles = getCardRoles(execution, assignment.card)
                def currentCardRole = cardRoles.find {CardRole cr -> cr.user == assignment.user}
                def nextCardRoles = []
                int nextSortOrder = Integer.MAX_VALUE

                //finding cardRoles with next sortOrder (next sort order can be current+1 or current+2, etc.
//                we don't know exactly)
                cardRoles.each {CardRole  cr ->
                    if (cr.sortOrder == nextSortOrder) {
                        nextCardRoles.add(cr)
                    } else if ((cr.sortOrder < nextSortOrder) && (cr.sortOrder > currentCardRole.sortOrder)) {
                        nextSortOrder = cr.sortOrder
                        nextCardRoles = [cr]
                    }
                }

//                def nextCardRoles = cardRoles.findAll {CardRole cr -> cr.sortOrder == currentCardRole.sortOrder + 1}
                if (nextCardRoles.isEmpty()) {
                    log.debug("Last user assignment finished, taking $signalName")

                    es.signalExecutionById(execution.getId(), signalName, params)
                    afterSignal(execution, signalName, parameters)
                } else {
                    log.debug("Creating assignments for group of users # ${currentCardRole.sortOrder + 1} in card role $role")
                    nextCardRoles.each {CardRole cr -> createUserAssignment(execution, assignment.card, cr, assignment.masterAssignment)}
                    execution.waitForSignal()
                }
            }
        }
    }

    protected List<Assignment> getSiblings(Assignment assignment) {
        EntityManager em = PersistenceProvider.getEntityManager()
        Query q = em.createQuery('''
                select a from wf$Assignment a
                where a.masterAssignment.id = ?1 and a.id <> ?2
              ''')
        q.setParameter(1, assignment.getMasterAssignment().getId())
        q.setParameter(2, assignment.getId())
        List<Assignment> siblings = q.getResultList()
        return siblings
    }

    protected List<CardRole> getCardRoles(ActivityExecution execution, Card card, Integer sortOrder) {
        def cardRoles = getCardRoles(execution, card)
        def cardRolesBySortOrder = cardRoles.findAll {CardRole cr -> cr.sortOrder == sortOrder}
        return cardRolesBySortOrder
    }

    protected void onSuccess(ActivityExecution execution, String signalName, Assignment assignment) {
    }
}