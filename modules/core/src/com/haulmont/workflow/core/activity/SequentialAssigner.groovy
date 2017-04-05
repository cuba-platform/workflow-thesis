/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.activity

import org.jbpm.api.activity.ActivityExecution
import com.google.common.base.Preconditions
import org.apache.commons.lang.StringUtils
import com.haulmont.cuba.core.EntityManager
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.workflow.core.exception.WorkflowException
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.WfHelper
import org.jbpm.api.ExecutionService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class SequentialAssigner extends MultiAssigner {

  private static Log log = LogFactory.getLog(SequentialAssigner.class)

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

    Assignment master = metadata.create(Assignment.class)
    master.setName(execution.getActivityName())
    master.setJbpmProcessId(execution.getProcessInstance().getId())
    master.setCard(card)
    em.persist(master)

    CardRole cr = cardRoles[0]
    createUserAssignment(execution, card, cr, master)
    return true
  }

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    if (parameters == null)
      throw new RuntimeException('Assignment object expected')
    Preconditions.checkState(persistence.isInTransaction(), 'An active transaction required')
    Preconditions.checkArgument(!StringUtils.isBlank(successTransition), 'successTransition is blank')

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
      ExecutionService es = WfHelper.getEngine().getProcessEngine().getExecutionService()
      Map<String, Object> params = new HashMap<String, Object>()
      params.put("assignment", assignment.getMasterAssignment())

      if (!successTransitions.contains(signalName)) {
        log.debug("Non-success transition has taken, signal master")
        es.signalExecutionById(execution.getId(), signalName, params)
        afterSignal(execution, signalName, parameters)
      } else {
        onSuccess(execution, signalName, assignment)

        log.debug("Looking for the next user")
        Card card = assignment.card
        List<CardRole> cardRoles = getCardRoles(execution, card, false)

        CardRole nextCr = null
        cardRoles.eachWithIndex { CardRole cr, int idx ->
          if (cr.user == assignment.user && idx < cardRoles.size() - 1) {
              nextCr = cardRoles[idx + 1]
          }
        }

        if (nextCr == null) {
          log.debug("Last user assignment finished, taking $signalName")
          es.signalExecutionById(execution.getId(), signalName, params)
          afterSignal(execution, signalName, parameters)
        } else {
          log.debug("Next assigned user: $nextCr.user")
          createUserAssignment(execution, card, nextCr, assignment.getMasterAssignment())
          execution.waitForSignal()
        }
      }
    }
  }

  protected void onSuccess(ActivityExecution execution, String signalName, Assignment assignment) {
  }

  protected def createUserAssignment(ActivityExecution execution, Card card, CardRole cr, Assignment master) {
    EntityManager em = persistence.getEntityManager()

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

    createTimers(execution, assignment, cr)

    em.persist(assignment)

    notifyUser(execution, card, [(assignment): cr], getNotificationState(execution))
  }
}
