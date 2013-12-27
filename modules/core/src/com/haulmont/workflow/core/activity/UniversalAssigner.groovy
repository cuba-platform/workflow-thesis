/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */



package com.haulmont.workflow.core.activity

import com.google.common.base.Preconditions
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.TimeProvider
import com.haulmont.workflow.core.WfHelper
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.WorkflowException
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.ExecutionService
import org.jbpm.api.activity.ActivityExecution

/**
 *
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class UniversalAssigner extends MultiAssigner {

    private Log log = LogFactory.getLog(Assigner.class)
    protected Persistence persistence = AppBeans.get(Persistence.NAME);
    String statusForFinish
    def statusesForFinish = []
    Boolean finishBySingleUser

    @Override
    protected boolean createAssignment(ActivityExecution execution) {

        EntityManager em = persistence.getEntityManager()

        Card card = findCard(execution)

        List<CardRole> srcCardRoles = getCardRoles(execution, card)
        def cardRoles = []
        if (srcCardRoles) {
            int minSortOrder = srcCardRoles[0].sortOrder;
            cardRoles = srcCardRoles.findAll { CardRole cr -> cr.sortOrder == minSortOrder }
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

        Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

        Assignment master = metadata.create(Assignment.class)
        master.setName(execution.getActivityName())
        master.setJbpmProcessId(execution.getProcessInstance().getId())
        master.setCard(card)
        em.persist(master)

        createUserAssignments(execution, card, master, cardRoles)

        return true
    }

    @Override
    void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
        if (parameters == null)
            throw new RuntimeException('Assignment object expected')
        Preconditions.checkState(persistence.isInTransaction(), 'An active transaction required')

        Assignment assignment = (Assignment) parameters.get("assignment")

        if (assignment.getMasterAssignment() == null) {
            log.debug("No master assignment, just taking $signalName")
            assignment.setFinished(TimeProvider.currentTimestamp());
            execution.take(signalName)
            removeTimers(execution)
            onSuccess(execution, signalName, assignment)
            afterSignal(execution, signalName, parameters)
        } else {
            //todo change log message
            log.debug("Trying to finish assignment")

            onSuccess(execution, signalName, assignment)

            def siblings = getSiblings(assignment)
            if (finishBySingleUser && statusesForFinish.contains(signalName))
                finishSiblings(assignment, siblings)

            String resultTransition = signalName
            for (Assignment sibling: siblings) {
                if (!sibling.finished) {
                    log.debug("Parallel assignment is not finished: assignment.id=${sibling.id}")
                    execution.waitForSignal()
                    removeTimers(execution, assignment)
                    return
                }

                if (sibling.getOutcome() != null && !successTransitions.contains(sibling.getOutcome()))
                    resultTransition = sibling.getOutcome()
            }
            processSignal(assignment, resultTransition, execution, signalName, parameters)
            removeTimers(execution, assignment)
        }
    }

    protected void processSignal(Assignment assignment, String resultTransition, ActivityExecution execution, String signalName, Map<String, ? extends Object> parameters) {
        ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
        Map<String, Object> params = new HashMap<String, Object>()
        params.put("assignment", assignment.getMasterAssignment())

        if (!successTransitions.contains(resultTransition)) {
            processNotSuccessfullTransition(assignment, resultTransition, execution, signalName, parameters)
        } else {
            processSuccessfullTransition(assignment, resultTransition, execution, signalName, parameters)
        }
    }

    protected void processNotSuccessfullTransition(Assignment assignment, String resultTransition, ActivityExecution execution, String signalName, Map<String, ? extends Object> parameters) {
        ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
        Map<String, Object> params = new HashMap<String, Object>()
        params.put("assignment", assignment.getMasterAssignment())

        log.debug("Non-success transition has taken, signal master")
        setRoleIds(assignment.card, null)
        es.signalExecutionById(execution.getId(), resultTransition, params)
        afterSignal(execution, signalName, parameters)
    }

    protected void processSuccessfullTransition(Assignment assignment, String resultTransition, ActivityExecution execution, String signalName, Map<String, ? extends Object> parameters) {
        ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
        Map<String, Object> params = new HashMap<String, Object>()
        params.put("assignment", assignment.getMasterAssignment())

        def cardRoles = getCardRoles(execution, assignment.card)
        List<UUID> ids = getRoleIds(assignment.card);
        def currentCardRole = cardRoles.find { CardRole cr -> cr.user == assignment.user && (ids.contains(cr.id) || ids.isEmpty()) }
        //Use for processes where variable "cardRoleUuids" is not correctly cleared after not-success transition
        if (currentCardRole == null)
            currentCardRole = cardRoles.find { CardRole cr -> cr.user == assignment.user }

        def nextCardRoles = []
        int nextSortOrder = Integer.MAX_VALUE

        //finding cardRoles with next sortOrder (next sort order can be current+1 or current+2, etc.
        //                we don't know exactly)
        cardRoles.each { CardRole cr ->
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
            setRoleIds(assignment.card, null)
            es.signalExecutionById(execution.getId(), signalName, params)
            afterSignal(execution, signalName, parameters)
        } else {
            log.debug("Creating assignments for group of users # ${currentCardRole.sortOrder + 1} in card role $role")
            setRoleIds(assignment.card, nextCardRoles);
            createUserAssignments(execution, assignment.card, assignment.masterAssignment, nextCardRoles)
            execution.waitForSignal()
        }
    }

    protected List<UUID> getRoleIds(Card card) {
        List<UUID> ids = (List<UUID>) WfHelper.getExecutionService().getVariable(card.jbpmProcessId, "cardRoleUuids")
        return ids ? ids : Collections.<UUID> emptyList()
    }

    protected void setRoleIds(Card card, List<CardRole> cardRoles) {
        List<UUID> ids = null;
        if (cardRoles) {
            ids = new ArrayList<UUID>(cardRoles.size())
            cardRoles.each { CardRole cr -> ids.add(cr.id) }
        } else
            ids = new ArrayList<UUID>();
        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            WfHelper.getExecutionService().setVariable(card.jbpmProcessId, "cardRoleUuids", ids)
            tx.commit();
        }
        finally {
            tx.end();
        }
    }

    protected List<Assignment> getSiblings(Assignment assignment) {
        EntityManager em = persistence.getEntityManager()
        Query q = em.createQuery('''
                select a from wf$Assignment a
                where a.masterAssignment.id = ?1 and a.id <> ?2
              ''')
        q.setParameter(1, assignment.getMasterAssignment().getId())
        q.setParameter(2, assignment.getId())
        List<Assignment> siblings = q.getResultList()
        return siblings
    }

    protected void finishSiblings(Assignment assignment, List<Assignment> siblings) {
        for (Assignment sibling: siblings) {
            sibling.setFinished(assignment.getFinished())
            sibling.setFinishedByUser(assignment.getFinishedByUser())
            sibling.setOutcome(assignment.getOutcome())

            deleteNotifications(sibling);
        }
    }

    protected void deleteNotifications(Assignment assignment) {
        EntityManager em = persistence.getEntityManager();
        Query query = em.createQuery("update wf\$CardInfo ci set ci.deleteTs = ?1, ci.deletedBy = ?2 " +
                "where ci.card.id = ?3 and ci.user.id = ?4");
        query.setParameter(1, assignment.finished);
        query.setParameter(2, assignment.finishedByUser.login);
        query.setParameter(3, assignment.card.id);
        query.setParameter(4, assignment.user.id);
        query.executeUpdate()
    }

    protected void createUserAssignments(ActivityExecution execution, Card card, Assignment master, Collection<CardRole> cardRoles) {
        Map<Assignment, CardRole> assignmentCardRoleMap = new HashMap<Assignment, CardRole>();
        Persistence persistence = AppBeans.get(Persistence.NAME);
        for (CardRole cr: cardRoles) {
            EntityManager em = persistence.entityManager;
            Assignment familyAssignment = findFamilyAssignment(card)
            Assignment assignment = metadata.create(Assignment.class)
            assignment.setName(execution.getActivityName())

            if (StringUtils.isBlank(description))
                assignment.setDescription('msg://' + execution.getActivityName())
            else
                assignment.setDescription(description)

            assignment.setJbpmProcessId(execution.getProcessInstance().getId())
            assignment.setCard(card)
            assignment.setProc(card.getProc())
            assignment.setUser(cr.user)
            assignment.setMasterAssignment(master)
            assignment.setIteration(calcIteration(card, cr.user, execution.getActivityName()))
            assignment.setFamilyAssignment(familyAssignment)

            createTimers(execution, assignment, cr)
            em.persist(assignment)

            assignmentCardRoleMap.put(assignment, cr);
        }

        NotificationMatrixAPI notificationMatrix = AppBeans.get(NotificationMatrixAPI.NAME)
        notificationMatrix.notifyByCardAndAssignments(card, assignmentCardRoleMap, notificationState)
    }

    protected List<CardRole> getCardRoles(ActivityExecution execution, Card card, Integer sortOrder) {
        def cardRoles = getCardRoles(execution, card)
        def cardRolesBySortOrder = cardRoles.findAll { CardRole cr -> cr.sortOrder == sortOrder }
        return cardRolesBySortOrder
    }

    protected void onSuccess(ActivityExecution execution, String signalName, Assignment assignment) {
    }

    public void setStatusesForFinish(String value) {
        statusForFinish = value
        if (statusForFinish) {
            String[] parts = statusForFinish.split('[;,]')
            statusesForFinish.addAll(parts)
        }
    }

}