/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:14:19
 *
 * $Id$
 */
package workflow.activity

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

public class Assigner extends CardActivity implements ExternalActivityBehaviour {

  private Log log = LogFactory.getLog(Assigner.class)

  String assignee
  String role
  String description
  String notify
  String notificationScript
  AssignmentTimersFactory timersFactory

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

    notificationMatrix.notifyByCard(card,notificationState, role)
    if (!isBlank(assignee)) {
      Query q = em.createQuery('select u from sec$User u where u.loginLowerCase = ?1')
      q.setParameter(1, assignee.toLowerCase())
      List<User> list = q.getResultList()
      if (list.isEmpty())
        throw new RuntimeException('User not found: ' + assignee)
      cr = null
      user = list.get(0)
    } else {
      cr = card.getRoles().find { CardRole it -> it.procRole.code == role && card.proc == it.procRole.proc}
      if (!cr)
        throw new WorkflowException(WorkflowException.Type.NO_CARD_ROLE,
                "User not found: cardId=${card.getId()}, procRole=$role", role)
      user = cr.getUser()
    }

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

    if (timersFactory) {
      timersFactory.createTimers(execution, assignment)
    }

    em.persist(assignment)

    notificationMatrix.notifyByAssignment(assignment, cr, notificationState)

    afterCreateAssignment(assignment)

    return true
  }

  protected void afterCreateAssignment(Assignment assignment) {}

  public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
    execution.take(signalName)
    if (timersFactory) {
      timersFactory.removeTimers(execution)
    }
  }

  protected Integer calcIteration(Card card, User user, String activityName) {
    EntityManager em = PersistenceProvider.getEntityManager()
    Query q = em.createQuery(
            'select max(a.iteration) from wf$Assignment a where a.card.id = ?1 and a.user.id = ?2 and a.name = ?3')
    q.setParameter(1, card.id)
    q.setParameter(2, user.id)
    q.setParameter(3, activityName)
    Object result = q.getSingleResult()
    if (result)
      return result + 1
    else
      return 1
  }
}
