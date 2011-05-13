/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.11.2009 11:43:34
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.LookupField
import com.haulmont.cuba.gui.data.CollectionDatasource

import com.haulmont.cuba.gui.data.CollectionDatasourceListener.Operation
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter
import com.haulmont.workflow.web.ui.base.AbstractCardEditor
import com.haulmont.cuba.gui.components.Table
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper

public class CardEditor extends AbstractCardEditor {

  def CardEditor(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
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
                      { CollectionDatasource ds, Operation operation ->
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