/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Metadata
import com.haulmont.cuba.core.global.TimeSource
import com.haulmont.cuba.security.entity.User
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.app.WfAssignmentWorker
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.timer.AssignmentTimersFactory
import com.haulmont.workflow.core.timer.OverdueAssignmentTimersFactory
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.api.activity.ExternalActivityBehaviour

import static com.google.common.base.Preconditions.checkState
import static org.apache.commons.lang.StringUtils.isBlank

public class Assigner extends CardActivity implements ExternalActivityBehaviour, HasTimersFactory {

    private Log log = LogFactory.getLog(Assigner.class)

    String assignee
    String role
    String description
    String notify
    String notificationScript
    AssignmentTimersFactory timersFactory
    Metadata metadata = AppBeans.get(Metadata.NAME)
    Persistence persistence = AppBeans.get(Persistence.NAME)
    TimeSource timeSource = AppBeans.get(TimeSource.NAME)
    WfAssignmentWorker wfAssignmentWorker = AppBeans.get(WfAssignmentWorker.NAME)

    public void execute(ActivityExecution execution) throws Exception {
        checkState(!(isBlank(assignee) && isBlank(role)), 'Both assignee and role are blank')
        checkState(persistence.isInTransaction(), 'An active transaction required')
        delayedNotify = true
        super.execute(execution)
        if (createAssignment(execution))
            execution.waitForSignal()
    }

    protected boolean createAssignment(ActivityExecution execution) {
        EntityManager em = persistence.getEntityManager()

        CardRole cr
        User user
        Card card = findCard(execution)

        if (!isBlank(assignee)) {
            Query q = em.createQuery('select u from sec$User u where u.loginLowerCase = ?1')
            q.setParameter(1, assignee.toLowerCase())
            List<User> list = q.getResultList()
            if (list.isEmpty())
                throw new RuntimeException('User not found: ' + assignee)
            cr = null
            user = list.get(0)
        } else {
            Collection<CardRole> cardRoles = card.getRoles().findAll { CardRole it -> it.procRole.code == role && card.proc == it.procRole.proc }
            if (!cardRoles || cardRoles.empty) {
                def pr = getProcRoleByCode(card, role)
                throw new WorkflowException(WorkflowException.Type.NO_CARD_ROLE,
                        "User not found: cardId=${card.getId()}, procRole=$role", pr?.name ? pr.name : role)
            }
            if (execution.hasVariable("iteratedAssigner")) {
                String iteratedAssigner = execution.getVariable("iteratedAssigner");
                UUID id = UUID.fromString(iteratedAssigner)
                cr = cardRoles.find { CardRole it -> it.user != null && it.user.id == id };
                cr = cr ?: cardRoles.iterator().next();
                user = cr.getUser();
            } else {
                cr = cardRoles.iterator().next();
                user = cr.getUser()
            }
        }
        Assignment familyAssignment = findFamilyAssignment(card)
        createUserAssignment(execution, card, cr,
                calcIteration(card, user, execution.getActivityName()),
                getDescription("${execution.getActivityName()}.description", description),
                familyAssignment, null, true);
        return true
    }

    protected void notifyUser(ActivityExecution execution, Card card, Map<Assignment, CardRole> assignmentCardRoleMap,
                              String state) {
        if (!notificationMatrix)
            notificationMatrix = AppBeans.get(NotificationMatrixAPI.NAME)
        notificationMatrix.notifyByCardAndAssignments(card, assignmentCardRoleMap, state)
    }

    protected void afterCreateAssignment(Assignment assignment) {}

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        execution.take(signalName)
        removeTimers(execution, null)
        afterSignal(execution, signalName, parameters)
    }

    protected Integer calcIteration(Card card, User user, String activityName) {
        EntityManager em = persistence.getEntityManager()
        Query q = em.createQuery('''select max(a.iteration) from wf$Assignment a where a.card.id = ?1 and
                                a.user.id = ?2 and a.name = ?3 and a.proc.id = ?4''')
        q.setParameter(1, card.id)
        q.setParameter(2, user.id)
        q.setParameter(3, activityName)
        q.setParameter(4, card.proc)
        Object result = q.getSingleResult()
        if (result)
            return result + 1
        else
            return 1
    }

    protected ProcRole getProcRoleByCode(Card card, String roleCode) {
        EntityManager em = persistence.getEntityManager()
        Query query = em.createQuery('select pr from wf$ProcRole pr where pr.proc.id = :proc and pr.code = :code')
        query.setParameter('proc', card.proc)
        query.setParameter('code', roleCode)
        List<ProcRole> list = query.getResultList()
        if (!list.isEmpty())
            return list[0]
        else
            return null;
    }

    protected def createUserAssignment(ActivityExecution execution, Card card, CardRole cr, Assignment master) {
        createUserAssignment(execution, card, cr, null, master, true);
    }

    protected Assignment createUserAssignment(ActivityExecution execution, Card card, CardRole cr,
                                              Assignment familyAssignment, Assignment master) {
        return createUserAssignment(execution, card, cr, familyAssignment, master, true);
    }

    protected Assignment createUserAssignment(ActivityExecution execution, Card card, CardRole cr,
                                              Assignment familyAssignment, Assignment master, boolean isNotify) {
        return createUserAssignment(execution, card, cr,
                calcIteration(card, cr.user, execution.getActivityName()),
                getDescription(execution.getActivityName(), description), familyAssignment, master, isNotify);
    }

    protected Assignment createUserAssignment(ActivityExecution execution, Card card, CardRole cr,
                                              Integer iteration, String description, Assignment familyAssignment,
                                              Assignment master, boolean isNotify) {
        EntityManager em = persistence.getEntityManager()

        Assignment assignment = createNewUserAssignment(execution, card, cr, iteration, description, familyAssignment, master);
        createTimers(execution, assignment, cr)
        em.persist(assignment)
        afterCreateAssignment(assignment)

        if (isNotify) notifyUser(execution, card, [(assignment): cr], getNotificationState(execution))
        return assignment;
    }

    protected def createNewUserAssignment(ActivityExecution execution, Card card, CardRole cr,
                                          Integer iteration, String description, Assignment familyAssignment, Assignment master) {
        return wfAssignmentWorker.createAssignment(
                execution.getActivityName(),
                cr,
                description,
                execution.getProcessInstance().getId(),
                cr.user,
                card,
                card.getProc(),
                iteration,
                familyAssignment,
                master)
    }

    protected Assignment createMasterAssignment(ActivityExecution execution, Card card) {
        Assignment master = metadata.create(Assignment.class)
        master.setName(execution.getActivityName())
        master.setJbpmProcessId(execution.getProcessInstance().getId())
        master.setCard(card)
        return master;
    }

    protected void createTimers(ActivityExecution execution, Assignment assignment, CardRole cardRole) {
        if (timersFactory) {
            timersFactory.createTimers(execution, assignment)
        }

        if (cardRole != null && cardRole.duration && cardRole.timeUnit && assignment.proc.durationEnabled) {
            new OverdueAssignmentTimersFactory().createTimers(execution, assignment)
        }
    }

    protected void removeTimers(ActivityExecution execution) {
        if (timersFactory)
            timersFactory.removeTimers(execution)

        new OverdueAssignmentTimersFactory().removeTimers(execution)
    }

    protected void removeTimers(ActivityExecution execution, Assignment assignment) {
        if (timersFactory)
            timersFactory.removeTimers(execution, assignment)

        new OverdueAssignmentTimersFactory().removeTimers(execution, assignment);
    }

    protected Assignment findFamilyAssignment(Card card) {
        if (card.procFamily != null) {
            EntityManager em = persistence.getEntityManager();
            Query q = em.createQuery("select a from wf\$Assignment a where a.subProcCard.id = ?1").setParameter(1, card.id)
            List<Assignment> resultList = q.getResultList();
            return resultList.isEmpty() ? null : resultList.get(0);
        }
    }

    protected String getDescription(String messageName, String description) {
        if (StringUtils.isBlank(description))
            return ("msg://" + messageName);
        return (description);
    }
}
