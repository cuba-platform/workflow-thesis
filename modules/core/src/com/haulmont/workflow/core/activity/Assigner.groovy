/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.Locator
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.cuba.core.Query

import com.haulmont.cuba.security.entity.User
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.timer.AssignmentTimersFactory
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.api.activity.ExternalActivityBehaviour
import static com.google.common.base.Preconditions.checkState
import static org.apache.commons.lang.StringUtils.isBlank
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.workflow.core.entity.ProcStage

import com.haulmont.workflow.core.app.WorkCalendarAPI
import com.haulmont.cuba.core.global.TimeProvider

import com.haulmont.workflow.core.entity.CardStage
import com.haulmont.cuba.core.global.ScriptingProvider
import com.haulmont.workflow.core.global.TimeUnit
import com.haulmont.workflow.core.app.NotificationMatrixAPI
import com.haulmont.workflow.core.timer.OverdueAssignmentTimersFactory
import com.haulmont.cuba.core.global.EntityLoadInfo

public class Assigner extends CardActivity implements ExternalActivityBehaviour {

  private Log log = LogFactory.getLog(Assigner.class)

  String assignee
  String role
  String description
  String notify
  String notificationScript
  AssignmentTimersFactory timersFactory
  
  //we don't use this property in Assigner, but use 'duration' property in *.jpdl.xml when reading default stage durations
  //if duration definition will be moved to other place, then remove duration from here
  String duration

  public void execute(ActivityExecution execution) throws Exception {
    checkState(!(isBlank(assignee) && isBlank(role)), 'Both assignee and role are blank')
    checkState(Locator.isInTransaction(), 'An active transaction required')
    delayedNotify = true
    super.execute(execution)
    if (createAssignment(execution))
      execution.waitForSignal()
  }

  protected boolean createAssignment(ActivityExecution execution) {
    EntityManager em = PersistenceProvider.getEntityManager()

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
      Collection<CardRole> cardRoles = card.getRoles().findAll { CardRole it -> it.procRole.code == role && card.proc == it.procRole.proc}
      if (!cardRoles || cardRoles.empty) {
        def pr = getProcRoleByCode(card, role)
        throw new WorkflowException(WorkflowException.Type.NO_CARD_ROLE,
                "User not found: cardId=${card.getId()}, procRole=$role", pr?.name ? pr.name : role)
      }
      if (execution.hasVariable("iteratedAssigner"))
      {
        String iteratedAssigner = execution.getVariable("iteratedAssigner");
        UUID id = UUID.fromString(iteratedAssigner)
        cr = cardRoles.find {CardRole it -> it.user != null && it.user.id == id};
        cr = cr ?: cardRoles.iterator().next();
        user = cr.getUser();
      }
      else {
        cr = cardRoles.iterator().next();
        user = cr.getUser()
      }
    }

    Assignment familyAssignment = findFamilyAssignment(card)
    Assignment assignment = new Assignment()
    assignment.setName(execution.getActivityName())

    if (StringUtils.isBlank(description))
      assignment.setDescription("msg://${execution.getActivityName()}.description")
    else
      assignment.setDescription(description)

    assignment.setJbpmProcessId(execution.getProcessInstance().getId())
    assignment.setUser(user)
    assignment.setCard(card)
    assignment.setProc(card.getProc())
    assignment.setIteration(calcIteration(card, user, execution.getActivityName()))
    assignment.setFamilyAssignment(familyAssignment)

    createTimers(execution, assignment, cr)

    createStages(assignment)

    em.persist(assignment)

    notifyUser(execution, card, [(assignment): cr], notificationState)

    afterCreateAssignment(assignment)

    return true
  }

    protected void notifyUser(
            ActivityExecution execution,
            Card card,
            Map<Assignment, CardRole> assignmentCardRoleMap,
            String state
    ) {
        if (!notificationMatrix)
            notificationMatrix = Locator.lookup(NotificationMatrixAPI.NAME)
        notificationMatrix.notifyByCardAndAssignments(card, assignmentCardRoleMap, state)
    }

  protected void afterCreateAssignment(Assignment assignment) {}

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    execution.take(signalName)
    removeTimers(execution, null)
    Card card = findCard(execution)
    finishStages(card, execution, signalName)

    afterSignal(execution, signalName, parameters)
  }

  protected Integer calcIteration(Card card, User user, String activityName) {
    EntityManager em = PersistenceProvider.getEntityManager()
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

  protected ProcRole getProcRoleByCode (Card card, String roleCode) {
    EntityManager em = PersistenceProvider.getEntityManager()
    Query query = em.createQuery('select pr from wf$ProcRole pr where pr.proc.id = :proc and pr.code = :code')
    query.setParameter('proc', card.proc)
    query.setParameter('code', roleCode)
    List<ProcRole> list = query.getResultList()
    if (!list.isEmpty())
      return list[0]
    else
      return null;
  }

  protected void createStages(Assignment assignment) {
    Card card = assignment.card
    EntityManager em = PersistenceProvider.getEntityManager()
    def query = em.createQuery('select ps from wf$ProcStage ps where ps.proc.id = :proc and ps.startActivity = :activityName')
    query.setParameter('proc', card.proc)
    query.setParameter('activityName', assignment.name)

    WorkCalendarAPI workCalendarAPI = Locator.lookup(WorkCalendarAPI.NAME)
    List<ProcStage> list = query.getResultList()
    Date currentTimestamp = TimeProvider.currentTimestamp()
    list.each{ProcStage ps ->
      Date dueDate = null
      Map durationMap = getStageDuration(card, ps)
      int duration = durationMap['duration']
      TimeUnit timeUnit = durationMap['timeUnit']
      if (ps.startActivity == ps.endActivity) {
        dueDate = workCalendarAPI.addInterval(currentTimestamp, duration, timeUnit)
        assignment.dueDate = dueDate
      }

      CardStage cardStage = card.stages.find{CardStage cs -> cs.procStage == ps && cs.endDateFact == null}
      if (cardStage == null) {
        cardStage = new CardStage()
        cardStage.setCard(card)
        cardStage.setStartDate(currentTimestamp)
        if (dueDate == null)
          dueDate = workCalendarAPI.addInterval(currentTimestamp, duration, timeUnit)
        cardStage.setEndDatePlan(dueDate)
        cardStage.setProcStage(ps)
        em.persist(cardStage)
      }

    }
  }

  protected Map getStageDuration(Card card, ProcStage procStage) {
    if (procStage.durationScriptEnabled && (procStage.durationScript != null)) {
      Binding binding = new Binding()
      binding.setVariable('card', card)
      ScriptingProvider.evaluateGroovy(Layer.CORE, procStage.durationScript, binding)
      return binding.getVariables()
    }

    return ['duration' : procStage.duration, 'timeUnit' : procStage.timeUnit]
  }

  protected void finishStages(Card card, ActivityExecution execution, String transition) {
    def currentDate = TimeProvider.currentTimestamp()
    card.stages.each{CardStage cs ->
      List<String> endTransitionList = []
      if (cs.procStage.endTransition) {
        endTransitionList = cs.procStage.endTransition.split(',');
      }
      if (cs.procStage.endActivity == execution.activityName
          && (cs.procStage.endTransition == null || endTransitionList.contains(transition))
          && !cs.endDateFact) {
        cs.endDateFact = currentDate
      }
    }
  }

    protected def createUserAssignment(ActivityExecution execution, Card card, CardRole cr, Assignment master) {
      EntityManager em = PersistenceProvider.getEntityManager()

      Assignment familyAssignment = findFamilyAssignment(card)

      Assignment assignment = new Assignment()
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

      NotificationMatrixAPI  notificationMatrix = Locator.lookup(NotificationMatrixAPI.NAME)
      notificationMatrix.notifyByCardAndAssignments(card, [(assignment): cr], notificationState)
    }

    protected void createTimers(ActivityExecution execution, Assignment assignment, CardRole cardRole) {
        if (timersFactory) {
            timersFactory.createTimers(execution, assignment)
        }

        if (cardRole.duration && cardRole.timeUnit) {
            WorkCalendarAPI workCalendar = Locator.lookup(WorkCalendarAPI.NAME);
            def dueDate = workCalendar.addInterval(TimeProvider.currentTimestamp(), cardRole.duration, cardRole.timeUnit)
            def overdueAssignmentTimersFactory = new OverdueAssignmentTimersFactory(dueDate)
            overdueAssignmentTimersFactory.setDueDate(dueDate)
            assignment.dueDate = dueDate

            EntityLoadInfo crLoadInfo = EntityLoadInfo.create(cardRole);
            overdueAssignmentTimersFactory.createTimers(execution, assignment, ['cardRole' : crLoadInfo.toString()])
        }
    }

    protected void removeTimers(ActivityExecution execution) {
        if (timersFactory)
            timersFactory.removeTimers(execution)

        new OverdueAssignmentTimersFactory().removeTimers(execution);
    }

    protected void removeTimers(ActivityExecution execution, Assignment assignment) {
        if (timersFactory)
            timersFactory.removeTimers(execution, assignment)

        new OverdueAssignmentTimersFactory().removeTimers(execution, assignment);
    }

    protected Assignment findFamilyAssignment(Card card)
    {
      if (card.procFamily != null) {
        EntityManager em = PersistenceProvider.getEntityManager();
        Query q = em.createQuery("select a from wf\$Assignment a where a.subProcCard.id = ?1").setParameter(1, card.id)
        List<Assignment> resultList = q.getResultList();
        return resultList.isEmpty() ? null : resultList.get(0);
      }
    }
}
