/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:19:07
 *
 * $Id$
 */
package com.haulmont.workflow.core.activity

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
import com.haulmont.cuba.core.global.TimeProvider

public class ParallelAssigner extends MultiAssigner {

  private Log log = LogFactory.getLog(ParallelAssigner.class)

  @Override
  protected boolean createAssignment(ActivityExecution execution) {
    Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

    EntityManager em = PersistenceProvider.getEntityManager()

    Card card = findCard(execution)

    List<CardRole> cardRoles = getCardRoles(execution, card)
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

    Assignment master = new Assignment()
    master.setName(execution.getActivityName())
    master.setJbpmProcessId(execution.getProcessInstance().getId())
    master.setCard(card)
    em.persist(master)

    Map<Assignment, CardRole> assignmentsCardRoleMap = new HashMap<Assignment,CardRole>();
    for (CardRole cr: cardRoles) {
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

      if (timersFactory) {
        timersFactory.createTimers(execution, assignment)
      }
      em.persist(assignment)

      assignmentsCardRoleMap.put(assignment, cr)
    }

    createStages(master);

    notificationMatrix.notifyByCardAndAssignments(card, assignmentsCardRoleMap, notificationState);
    return true
  }

  @Override
  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
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
    } else {
      log.debug("Trying to finish assignment with success outcome")

      onSuccess(execution, signalName, assignment)

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
      else {
        log.debug("All of parallel assignments have been finished successfully")
      }

      es.signalExecutionById(execution.getId(), resultTransition, params)
      Card card = findCard(execution)
      finishStages(card, execution, signalName)
      afterSignal(execution, signalName, parameters)
    }
  }

  protected void onSuccess(ActivityExecution execution, String signalName, Assignment assignment) {
  }
}
