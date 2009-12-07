/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 07.12.2009 9:43:08
 *
 * $Id$
 */
package workflow.client.web.ui.card

import com.haulmont.workflow.core.entity.Card
import com.haulmont.cuba.core.global.PersistenceHelper
import com.haulmont.workflow.web.ui.base.AbstractWfAccessData
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.cuba.gui.UserSessionClient
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.LoadContext.Query
import com.haulmont.cuba.gui.ServiceLocator

public class CardAccessData extends AbstractWfAccessData {

  private Card card
  private Boolean saveEnabled

  def CardAccessData(Map params) {
    super(params);
    card = params['param$item']
  }

  boolean getNotStarted() {
    if (PersistenceHelper.isNew(card))
      return true
    else
      return card.jbpmProcessId == null
  }

  public boolean getSaveEnabled() {
    if (saveEnabled == null) {
      if (card.jbpmProcessId == null)
        saveEnabled = true
      else {
        LoadContext ctx = new LoadContext(CardRole.class)
        Query q = ctx.setQueryString('select cr.id from wf$CardRole cr where cr.card.id = :card and cr.user.id = :user')
        q.addParameter('card', card.getId())
        q.addParameter('user', UserSessionClient.currentOrSubstitutedUserId())
        List list = ServiceLocator.getDataService().loadList(ctx)
        saveEnabled = !list.isEmpty()
      }
    }
    return saveEnabled
  }
}