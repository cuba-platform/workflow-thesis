/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.11.2009 10:53:31
 *
 * $Id$
 */
package workflow.client.web.ui.card

import com.haulmont.cuba.gui.components.AbstractWindow
import com.haulmont.cuba.gui.components.Table
import com.haulmont.cuba.gui.components.TableActionsHelper
import com.haulmont.cuba.gui.components.IFrame

public class CardBrowser extends AbstractWindow {

  def CardBrowser(IFrame iFrame) {
    super(iFrame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);
    Table table = getComponent("cardTable")
    TableActionsHelper helper = new TableActionsHelper(this, table)
    helper.createRefreshAction()
    helper.createCreateAction()
    helper.createEditAction()
    helper.createRemoveAction()
  }
}