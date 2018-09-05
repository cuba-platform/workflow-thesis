/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */


package com.haulmont.workflow.core.activity

import com.google.common.base.Preconditions
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.Transaction
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.WfHelper
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.ParallelAssignmentIsNotFinishedException
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.global.WfConstants
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.ExecutionService
import org.jbpm.api.activity.ActivityExecution

import java.sql.Timestamp

/**
 *
 *
 */
public class UniversalAssigner extends MultiAssigner {

    private Log log = LogFactory.getLog(Assigner.class)
    protected Persistence persistence = AppBeans.get(Persistence.NAME);
    String statusForFinish
    def statusesForFinish = []
    Boolean finishBySingleUser

    @Override
    protected boolean createAssignment(ActivityExecution execution) {
        Card card = findCard(execution)

        List<CardRole> srcCardRoles = getCardRoles(execution, card, true)
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

        Assignment master = getMasterAssignment(execution, card)
        createUserAssignments(execution, card, master, cardRoles)

        return true
    }

    protected Assignment getMasterAssignment(ActivityExecution execution, Card card) {
        EntityManager em = persistence.getEntityManager()

        Assignment master = createMasterAssignment(execution, card);
        master.setCard(card)
        em.persist(master)

        return master
    }

    @Override
    protected List<CardRole> getCardRoles(ActivityExecution execution, Card card, boolean transitionToState) {
        EntityManager em = persistence.getEntityManager();
        List<String> roles;
        if (role.contains(",")) {
            roles = Arrays.asList(role.split(","));
        } else {
            roles = new ArrayList<>();
            roles.add(role);
        }
        List<CardRole> cardRoles = em.createQuery('''select cr from wf$CardRole cr where
            cr.card.id = ?1 and
            cr.procRole.code in ?2 and
            cr.procRole.proc.id = ?3
            order by cr.sortOrder, cr.createTs''')
                .setParameter(1, card)
                .setParameter(2, roles)
                .setParameter(3, card.proc)
                .getResultList();

        if (forRefusedOnly(execution)) {
            if (!execution.getVariable("date")) {
                String state;
                if (transitionToState) {
                    state = em.createQuery("select b.name from wf\$Assignment b where b.card.id=?1 and b.proc.id=?2 and b.createTs = " +
                            "(select MAX(a.createTs) from wf\$Assignment a where a.card.id=?1 and a.proc.id=?2 and a.jbpmProcessId is not null)")
                            .setParameter(1, card)
                            .setParameter(2, card.proc)
                            .getFirstResult();
                } else {
                    state = em.createQuery("select b.name from wf\$Assignment b where b.card.id=?1 and b.proc.id=?2 and b.createTs = " +
                            "(select MAX(a.createTs) from wf\$Assignment a where a.card.id=?1 and a.proc.id=?2 and (a.outcome not in (?3) and a.outcome is not null " +
                            "or a.name = 'Started') and a.masterAssignment.id is null)")
                            .setParameter(1, card)
                            .setParameter(2, card.proc)
                            .setParameter(3, successTransitions)
                            .getFirstResult();
                }
                Timestamp date = em.createQuery("select max(b.createTs) from wf\$Assignment b " +
                        "where b.card.id=?1 and b.proc.id=?2 " +
                        "and b.name IN ('Started',?3) and b.outcome not in (?4) " +
                        "and b.createTs <= (select max(c.createTs) from wf\$Assignment c " +
                        "where c.card.id=?1 and c.proc.id=?2 and c.name = ?3)")
                        .setParameter(1, card)
                        .setParameter(2, card.proc)
                        .setParameter(3, state)
                        .setParameter(4, getExcludedOutcomes())
                        .getFirstResult();
                execution.createVariable("date", date)
            }

            cardRoles = cardRoles.findAll {
                CardRole cr -> cardRoleRefused(cr, card, roles, execution);
            }
        } else {
            if (execution.hasVariable("date")) {
                execution.removeVariable("date")
            }
        }
        return cardRoles
    }

    def cardRoleRefused(CardRole cr, Card card, List<String> roles, ActivityExecution execution) {
        if (!roles.contains(cr.procRole.code)) {
            return false;
        }

        List<Assignment> assignments = persistence.getEntityManager().createQuery("select a from wf\$Assignment a " +
                "where a.card.id = ?1 and a.user.id = ?2 and a.proc.id = ?3 and a.name = ?4 and " +
                "a.iteration >= ALL " +
                "(select b.iteration from wf\$Assignment b where b.card.id = ?1 and b.proc.id = ?3 " +
                "and b.user.id = ?2 and b.iteration is not null and b.createTs >= ?5) and " +
                "a.createTs >= ?5 " +
                "order by a.finished desc")
                .setParameter(1, card)
                .setParameter(2, cr.user)
                .setParameter(3, card.proc)
                .setParameter(4, execution.activityName)
                .setParameter(5, execution.getVariable("date"))
                .getResultList()

        return assignments.isEmpty() || !successTransitions.contains(assignments.get(0).outcome);
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    protected Collection<String> getExcludedOutcomes() {
        return [WfConstants.ACTION_REASSIGN];
    }

    @Override
    void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
        if (parameters == null)
            throw new RuntimeException('Assignment object expected')
        Preconditions.checkState(persistence.isInTransaction(), 'An active transaction required')

        Assignment assignment = (Assignment) parameters.get("assignment")

        if (assignment.getMasterAssignment() == null) {
            log.debug("No master assignment, just taking $signalName")
            assignment.setFinished(timeSource.currentTimestamp());
            execution.take(signalName)
            removeTimers(execution)
            onSuccess(execution, signalName, assignment)
            afterSignal(execution, signalName, parameters)
        } else {
            //todo change log message
            log.debug("Trying to finish assignment")

            onSuccess(execution, signalName, assignment)

            def siblings = getSiblings(assignment)
            if (finishBySingleUser)
                if (statusesForFinish.size() == 0 || statusesForFinish.contains(signalName))
                    finishSiblings(assignment, siblings)

            String resultTransition
            try {
                resultTransition = getResultTransition(signalName, siblings)
            } catch (ParallelAssignmentIsNotFinishedException e) {
                log.debug("Parallel assignment is not finished: assignment.id=${e.assignmentId}")
                execution.waitForSignal()
                removeTimers(execution, assignment)
                return
            }

            processSignal(assignment, resultTransition, execution, signalName, parameters)
            removeTimers(execution, assignment)
        }
    }

    protected String getResultTransition(String signalName, List<Assignment> siblings)
            throws ParallelAssignmentIsNotFinishedException {

        String resultTransition = signalName
        for (Assignment sibling : siblings) {
            if (!sibling.finished) {
                throw new ParallelAssignmentIsNotFinishedException(sibling.id)
            }

            if (isNeededToUseThisSiblingOutcome(sibling))
                resultTransition = sibling.getOutcome()
        }
        return resultTransition
    }

    protected boolean isNeededToUseThisSiblingOutcome(Assignment sibling) {
        return sibling.getOutcome() != null && !sibling.getOutcome().equals(WfConstants.ACTION_REASSIGN) && !successTransitions.contains(sibling.getOutcome());
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
        def cardRoles = getCardRoles(execution, assignment.card, false)
        if (forRefusedOnly(execution)) {
            if (cardRoles) {
                int minSortOrder = cardRoles[0].sortOrder;
                cardRoles = cardRoles.findAll { CardRole cr -> cr.sortOrder == minSortOrder }
            }
            if (cardRoles.isEmpty()) {
                setRoleIds(assignment.card, null)
                es.signalExecutionById(execution.getId(), signalName, params)
                afterSignal(execution, signalName, parameters)
            } else {
                setRoleIds(assignment.card, cardRoles);
                createUserAssignments(execution, assignment.card, assignment.masterAssignment, cardRoles)
                execution.waitForSignal()
            }
        } else {
            List<UUID> ids = getRoleIds(assignment.card);
            def currentCardRole = getCurrentCardRole(cardRoles, assignment, ids);

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
    }

    protected Object getCurrentCardRole(Collection<CardRole> cardRoles, Assignment assignment, Collection<UUID> ids) {
        def currentCardRole = cardRoles.find { CardRole cr -> cr.user == assignment.user && (ids.contains(cr.id) || ids.isEmpty()) }
        //Use for processes where variable "cardRoleUuids" is not correctly cleared after not-success transition
        if (currentCardRole == null)
            currentCardRole = cardRoles.find { CardRole cr -> cr.user == assignment.user }

        return currentCardRole
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
        Map<Assignment, CardRole> assignmentCardRoleMap = new HashMap<Assignment, CardRole>();
        Persistence persistence = AppBeans.get(Persistence.NAME);
        Assignment familyAssignment = findFamilyAssignment(card)
        for (CardRole cr : cardRoles) {

            EntityManager em = persistence.entityManager;
            Assignment assignment = wfAssignmentWorker.createAssignment(execution.getActivityName(),
                    cr, getDescription("${execution.getActivityName()}.description", description),
                    execution.getProcessInstance().getId(),
                    cr.user, card, card.getProc(), calcIteration(card, cr.user, execution.getActivityName()),
                    familyAssignment, master)
            createTimers(execution, assignment, cr)
            em.persist(assignment)

            assignmentCardRoleMap.put(assignment, cr);
        }
        return assignmentCardRoleMap;
    }

    protected List<CardRole> getCardRoles(ActivityExecution execution, Card card, Integer sortOrder, boolean transitionToState) {
        def cardRoles = getCardRoles(execution, card, transitionToState)
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