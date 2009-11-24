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

import com.haulmont.cuba.core.*
import com.haulmont.cuba.security.entity.User
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Card
import static com.google.common.base.Preconditions.checkState
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.api.activity.ExternalActivityBehaviour
import static org.apache.commons.lang.StringUtils.isBlank

import java.util.List
import java.util.Map

public class Assigner extends CardActivity implements ExternalActivityBehaviour {

    protected String assignee
    protected String role

    public void setAssignee(String assignee) {
        this.assignee = assignee
    }

    public void setRole(String role) {
        this.role = role
    }

    public void execute(ActivityExecution execution) throws Exception {
        checkState(!(isBlank(assignee) && isBlank(role)), 'Both assignee and role are blank')
        checkState(Locator.isInTransaction(), 'An active transaction required')
        super.execute(execution)
        createAssignment(execution)
        execution.waitForSignal()
    }

    protected void createAssignment(ActivityExecution execution) {
        EntityManager em = PersistenceProvider.getEntityManager()

        User user
        Card card = findCard(execution)

        if (!isBlank(assignee)) {
            Query q = em.createQuery('select u from sec$User u where u.loginLowerCase = ?1')
            q.setParameter(1, assignee.toLowerCase())
            List<User> list = q.getResultList()
            if (list.isEmpty())
                throw new RuntimeException('User not found: ' + assignee)
            user = list.get(0)
        } else {
            Query q = em.createQuery('select cr.user from wf$CardRole cr ' +
                    'where cr.card.id = ?1 and cr.procRole.code = ?2')
            q.setParameter(1, card.getId())
            q.setParameter(2, role)
            List<User> list = q.getResultList()
            if (list.isEmpty())
                throw new RuntimeException("User not found: cardId=${card.getId()}, procRole=$role")
            user = list.get(0)
        }

        Assignment assignment = new Assignment()
        assignment.setName(execution.getActivityName())
        assignment.setJbpmProcessId(execution.getProcessInstance().getId())
        assignment.setUser(user)
        assignment.setCard(card)

        em.persist(assignment)
    }

    public void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) throws Exception {
        execution.take(signalName)
    }
}
