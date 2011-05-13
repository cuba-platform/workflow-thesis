/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 04.02.11 14:06
 *
 * $Id$
 */

package com.haulmont.workflow.core.activity

import com.haulmont.workflow.core.entity.Card
import org.jbpm.api.activity.ActivityExecution
import java.util.regex.Matcher
import java.util.regex.Pattern
import com.haulmont.cuba.core.EntityManager
import com.haulmont.cuba.core.PersistenceProvider
import org.jbpm.api.Execution

class ActivityHelper {

  static Card findCard(Execution execution) {
    String key = null;

//    In case of parallel executions execution object does not have
//    filled key property. We must extract execution_key from id.
//    id format : {process_key}.{execution_key}.{id}
    if (execution.state == ActivityExecution.STATE_ACTIVE_CONCURRENT) {
      Matcher matcher = Pattern.compile("\\.(.*)\\.").matcher(execution.getId());
      if (matcher.find())
        key = matcher.group(1);
    } else {
      key = execution.getKey()
    }

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

}
