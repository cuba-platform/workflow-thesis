/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.LoadContext.Query
import com.haulmont.cuba.core.global.PersistenceHelper
import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.cuba.gui.UserSessionClient
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardRole
import com.haulmont.cuba.gui.WindowParams
import com.haulmont.workflow.gui.base.AbstractWfAccessData

/**
 * @author krivopustov
 * @version $Id$
 */
public class CardAccessData extends AbstractWfAccessData {

  private Card card
  private Boolean saveEnabled

  def CardAccessData(Map params) {
    super(params);
    card = WindowParams.ITEM.getEntity(params)
  }

  boolean getNotStarted() {
    if (PersistenceHelper.isNew(card))
      return true
    else
      return card.jbpmProcessId == null
  }

  public boolean getSaveAndCloseEnabled() {
    return false;
  }

  public boolean getSaveEnabled() {
    if (saveEnabled == null) {
      if (card.jbpmProcessId == null)
        saveEnabled = true
      else {
        LoadContext ctx = new LoadContext(CardRole.class)
        Query q = ctx.setQueryString('select cr.id from wf$CardRole cr where cr.card.id = :card and cr.user.id = :user')
        q.setParameter('card', card.getId())
        q.setParameter('user', UserSessionClient.currentOrSubstitutedUserId())
        List list = ServiceLocator.getDataService().loadList(ctx)
        saveEnabled = !list.isEmpty()
      }
    }
    return saveEnabled
  }

  public boolean getStartProcessEnabled() {
    return true;
  }
}