/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 03.11.2010 17:01:15
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.attachmenttypes

import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.components.TableActionsHelper
import com.haulmont.cuba.gui.WindowManager

class AttachmentTypesBrowser extends AbstractWindow{
  private Table table

  def AttachmentTypesBrowser(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);

    table = getComponent("table")
    TableActionsHelper helper = new TableActionsHelper(this, table)
    helper.createCreateAction(WindowManager.OpenType.DIALOG)
    helper.createEditAction(WindowManager.OpenType.DIALOG)
    helper.createRemoveAction()
  }
}
