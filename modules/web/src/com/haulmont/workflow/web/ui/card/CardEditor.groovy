/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.CollectionDatasourceListener.Operation
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.web.ui.base.AbstractCardEditor
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper

public class CardEditor extends AbstractCardEditor<Card> {

    public void init(Map<String, Object> params) {
    super.init(params);

    if (cardRolesFrame) {
      LookupField procLookup = getComponent('proc')
      procLookup.addListener(
              { Object source, String property, Object prevValue, Object value ->
                cardRolesFrame.procChanged(value)
                if (value)
                  cardRolesFrame.initDefaultActors(value)
              } as ValueListener
      )
      cardRolesDs.addListener(
              [
                      collectionChanged:
                      { CollectionDatasource ds, Operation operation, List items ->
                        procLookup.setEnabled(ds.getItemIds().isEmpty())
                      }
              ] as CollectionDsListenerAdapter
      )
    }
  }

  public void setItem(Entity item) {
    super.setItem(item);
    Table attachmentsTable = getComponent("attachmentsTable")
    if (attachmentsTable != null)
      AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
  }

  @Override
  protected boolean isCommentVisible() {
    return true
  }

}