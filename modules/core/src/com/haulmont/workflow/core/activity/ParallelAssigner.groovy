/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.activity

import com.google.common.base.Preconditions
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Query
import com.haulmont.workflow.core.WfHelper
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.ParallelAssignmentIsNotFinishedException
import com.haulmont.workflow.core.exception.WorkflowException
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.ExecutionService
import org.jbpm.api.activity.ActivityExecution

public class ParallelAssigner extends MultiAssigner {

    private Log log = LogFactory.getLog(ParallelAssigner.class)

    Boolean finishBySingleUser

    @Override
    protected boolean createAssignment(ActivityExecution execution) {
        Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

        EntityManager em = persistence.getEntityManager()

        Card card = findCard(execution)

        List<CardRole> cardRoles = getCardRoles(execution, card, true)
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

        Assignment master = createMasterAssignment(execution, card);
        em.persist(master)
        afterCreateAssignment(master)

        createUserAssignments(execution, card, master, cardRoles)
        return true
    }

    protected Map<Assignment, CardRole> createUserAssignments(ActivityExecution execution, Card card,
                                                              Assignment master, Collection<CardRole> cardRoles) {
        Map<Assignment, CardRole> assignmentCardRoleMap = createUserAssignmentsWithoutNotifying(execution, card,
                master, cardRoles)
        notifyUser(execution, card, assignmentCardRoleMap, getNotificationState(execution))
        return assignmentCardRoleMap
    }

    protected Map<Assignment, CardRole> createUserAssignmentsWithoutNotifying(ActivityExecution execution, Card card,
                                                                              Assignment master,
                                                                              Collection<CardRole> cardRoles) {
        Map<Assignment, CardRole> assignmentCardRoleMap = new HashMap<Assignment, CardRole>()
        Assignment familyAssignment = findFamilyAssignment(card)
        for (CardRole cr : cardRoles) {
            Assignment assignment = createUserAssignment(execution, card, cr, familyAssignment, master, false)
            assignmentCardRoleMap.put(assignment, cr)
        }
        return assignmentCardRoleMap
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        if (parameters == null)
            throw new RuntimeException('Assignment object expected')
        Preconditions.checkState(persistence.isInTransaction(), 'An active transaction required')

        Assignment assignment = (Assignment) parameters.get("assignment")

        if (assignment.getMasterAssignment() == null) {
            log.debug("No master assignment, just taking $signalName")
            assignment.setFinished(timeSource.currentTimestamp());
            execution.take(signalName)
            if (timersFactory) {
                timersFactory.removeTimers(execution)
            }
        } else {
            if (timersFactory) {
                timersFactory.removeTimers(execution, assignment)
            }
            log.debug("Trying to finish assignment with success outcome")

            onSuccess(execution, signalName, assignment)

            List<Assignment> siblings = getSiblings(assignment)
            if (finishBySingleUser)
                finishSiblings(assignment, siblings)

            String resultTransition
            try {
                resultTransition = getResultTransition(signalName, siblings)
            } catch (ParallelAssignmentIsNotFinishedException e) {
                log.debug("Parallel assignment is not finished: assignment.id=${e.assignmentId}")
                execution.waitForSignal()
                return
            }

            ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
            Map<String, Object> params = new HashMap<String, Object>()
            params.put("assignment", assignment.getMasterAssignment())

            if (!successTransitions.contains(resultTransition))
                log.debug("Some of parallel assignments have been finished unsuccessfully")
            else {
                log.debug("All of parallel assignments have been finished successfully")
            }

            es.signalExecutionById(execution.getId(), resultTransition, params)
            afterSignal(execution, signalName, parameters)
        }
    }

    protected List<Assignment> getSiblings(Assignment assignment) {
        EntityManager em = persistence.getEntityManager()
        Query q = em.createQuery('''
              select a from wf$Assignment a
              where a.masterAssignment.id = ?1 and a.id <> ?2
            ''', metadata.getExtendedEntities().getEffectiveClass(Assignment.class))
        q.setParameter(1, assignment.getMasterAssignment().getId())
        q.setParameter(2, assignment.getId())
        List<Assignment> siblings = q.getResultList()
        return siblings
    }

    protected void finishSiblings(Assignment assignment, List<Assignment> siblings) {
        for (Assignment sibling : siblings) {
            sibling.setFinished(assignment.getFinished())
            sibling.setFinishedByUser(assignment.getFinishedByUser())
            sibling.setOutcome(assignment.getOutcome())

            deleteNotifications(sibling);
        }
    }

    protected String getResultTransition(String signalName, List<Assignment> siblings)
            throws ParallelAssignmentIsNotFinishedException {

        String resultTransition = signalName
        for (Assignment sibling : siblings) {
            if (!sibling.finished) {
                throw new ParallelAssignmentIsNotFinishedException(sibling.id)
            }

            if (!successTransitions.contains(sibling.getOutcome()))
                resultTransition = sibling.getOutcome()
        }
        return resultTransition
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

    protected void onSuccess(ActivityExecution execution, String signalName, Assignment assignment) {
    }
}
