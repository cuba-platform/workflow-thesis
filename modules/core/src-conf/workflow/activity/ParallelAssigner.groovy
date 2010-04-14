/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:19:07
 *
 * $Id$
 */
package workflow.activity

import com.google.common.base.Preconditions
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.PersistenceProvider
import com.haulmont.cuba.core.Query
import com.haulmont.cuba.core.Locator

import com.haulmont.workflow.core.WfHelper
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.ExecutionService
import org.jbpm.api.activity.ActivityExecution

import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.timer.AssignmentTimersFactory

public class ParallelAssigner extends Assigner {

  private Log log = LogFactory.getLog(ParallelAssigner.class)

  String successTransition
  AssignmentTimersFactory timersFactory

  @Override
  protected void createAssignment(ActivityExecution execution) {
    Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

    EntityManager em = PersistenceProvider.getEntityManager()

    Card card = findCard(execution)

    Query q = em.createQuery('''
          select cr from wf$CardRole cr
          where cr.card.id = ?1 and cr.procRole.code = ?2
        ''')
    q.setParameter(1, card.getId())
    q.setParameter(2, role)
    List<CardRole> cardRoles = q.getResultList()
    if (cardRoles.isEmpty())
      throw new WorkflowException(WorkflowException.Type.NO_CARD_ROLE,
              "User not found: cardId=${card.getId()}, procRole=$role", role)

    Assignment master = new Assignment()
    master.setName(execution.getActivityName())
    master.setJbpmProcessId(execution.getProcessInstance().getId())
    master.setCard(card)
    em.persist(master)

    for (CardRole cr: cardRoles) {
      Assignment assignment = new Assignment()
      assignment.setName(execution.getActivityName())

      if (StringUtils.isBlank(description))
        assignment.setDescription('msg://' + execution.getActivityName())
      else
        assignment.setDescription(description)

      assignment.setJbpmProcessId(execution.getProcessInstance().getId())
      assignment.setCard(card)
      assignment.setUser(cr.user)
      assignment.setMasterAssignment(master)
      assignment.setIteration(calcIteration(card, cr.user, execution.getActivityName()))

      if (timersFactory) {
        timersFactory.createTimers(execution, assignment)
      }
      em.persist(assignment)

      if (cr.notifyByEmail && !StringUtils.isBlank(cr.user.email))
        sendEmail(assignment, cr.user)
    }
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    if (parameters == null)
      throw new RuntimeException('Assignment object expected')
    Preconditions.checkState(Locator.isInTransaction(), 'An active transaction required')

    Assignment assignment = (Assignment) parameters.get("assignment")

    if (assignment.getMasterAssignment() == null) {
      log.debug("No master assignment, just taking $signalName")
      execution.take(signalName)
      if (timersFactory) {
        timersFactory.removeTimers(execution)
      }
    } else {
      log.debug("Trying to finish assignment with success outcome")

      EntityManager em = PersistenceProvider.getEntityManager()
      Query q = em.createQuery('''
              select a from wf$Assignment a
              where a.masterAssignment.id = ?1 and a.id <> ?2
            ''')
      q.setParameter(1, assignment.getMasterAssignment().getId())
      q.setParameter(2, assignment.getId())
      List<Assignment> siblings = q.getResultList()

      String resultTransition = signalName
      for (Assignment sibling: siblings) {
        if (sibling.getFinished() == null) {
          log.debug("Parallel assignment is not finished: assignment.id=${sibling.getId()}")
          execution.waitForSignal()
          return
        }
        if (sibling.getOutcome() != successTransition)
          resultTransition = sibling.getOutcome()
      }

      ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
      Map<String, Object> params = new HashMap<String, Object>()
      params.put("assignment", assignment.getMasterAssignment())

      if (resultTransition != successTransition)
        log.debug("Some of parallel assignments have been finished unsuccessfully")
      else
        log.debug("All of parallel assignments have been finished successfully")

      es.signalExecutionById(execution.getId(), resultTransition, params)
    }
  }
}
