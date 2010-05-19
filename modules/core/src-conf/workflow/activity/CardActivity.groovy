/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 23.11.2009 17:10:44
 *
 * $Id$
 */
package workflow.activity

import org.jbpm.api.activity.ActivityBehaviour
import org.jbpm.api.activity.ActivityExecution
import com.haulmont.workflow.core.entity.Card
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.PersistenceProvider
import org.apache.commons.lang.StringUtils
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.core.global.ScriptingProvider
import com.haulmont.cuba.core.app.EmailerAPI
import com.haulmont.cuba.core.Locator
import com.haulmont.cuba.core.app.EmailerMBean
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.haulmont.workflow.core.entity.CardProc
import com.haulmont.workflow.core.entity.Proc
import com.haulmont.workflow.core.entity.CardInfo

public class CardActivity implements ActivityBehaviour {

  String observers

  private Log log = LogFactory.getLog(CardActivity.class)

  public void execute(ActivityExecution execution) throws Exception {
    Card card = findCard(execution)

    card.setState(execution.getActivityName())

    CardProc cp = card.procs.find { it.proc == card.proc }
    cp?.setState(card.state)

    notifyObservers(card, execution)
  }

  protected Card findCard(ActivityExecution execution) {
    String key = execution.getKey()
    UUID cardId
    try {
      cardId = UUID.fromString(key)
    } catch (Exception e) {
      throw new RuntimeException('Unable to get cardId', e)
    }
    EntityManager em = PersistenceProvider.getEntityManager()
    Card card = em.find(Card.class, cardId)
    if (card == null)
      throw new RuntimeException("Card not found: $cardId")
    return card
  }

  protected void notifyObservers(Card card, ActivityExecution execution) {
    if (StringUtils.isBlank(observers))
      return

    List roleNames = observers.tokenize(',').collect { it.trim() }
    List<CardRole> cardRoles = card.getRoles().findAll { CardRole cr -> roleNames.contains(cr.procRole.code) }
    cardRoles.each { CardRole cr ->
      if (cr.notifyByEmail) {
        sendEmail(card, cr.user)
      }
      if (cr.notifyByCardInfo) {
        createNotificationCardInfo(card, cr.user, execution)
      }
    }
  }

  protected void sendEmail(Card card, User user) {
    String subject
    String body

    if (!user.email) return

    try {
      String script = card.proc.messagesPack.replace('.', '/') + '/ObserverNotification.groovy'
      Binding binding = new Binding(['card': card, 'user': user])
      ScriptingProvider.runGroovyScript(script, binding)
      subject = binding.getVariable('subject')
      body = binding.getVariable('body')
    } catch (Exception e) {
      log.warn("Unable to get email subject and body, using defaults", e)
      subject = "Notification: ${card.description} - ${card.locState}"
      body = """
Card ${card.description} has become ${card.locState}
"""
    }

    Thread.startDaemon('emailThread') {
      EmailerAPI emailer = Locator.lookup(EmailerAPI.NAME)
      emailer.sendEmail(user.email, subject, body)
    }
  }

  protected void createNotificationCardInfo(Card card, User user, ActivityExecution execution) {
    CardInfo ci = new CardInfo()
    ci.setType(CardInfo.TYPE_NOTIFICATION)
    ci.setCard(card)
    ci.setUser(user)
    ci.setActivity(execution.activityName)
    ci.setJbpmExecutionId(execution.id)

    String subject = getNotificationSubject(card, user)
    ci.setDescription(subject)

    EntityManager em = PersistenceProvider.getEntityManager()
    em.persist(ci)
  }

  protected String getNotificationSubject(Card card, User user) {
    String subject
    try {
      String script = card.proc.messagesPack.replace('.', '/') + '/ObserverNotification.groovy'
      Binding binding = new Binding(['card': card, 'user': user])
      ScriptingProvider.runGroovyScript(script, binding)
      subject = binding.getVariable('subject')
    } catch (Exception e) {
      log.warn("Unable to get notification text, using defaults", e)
      subject = "Notification: ${card.description} - ${card.locState}"
    }
    return subject
  }
}