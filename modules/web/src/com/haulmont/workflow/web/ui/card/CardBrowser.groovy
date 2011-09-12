/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 25.11.2009 10:53:31
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.card

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.WindowManager

public class CardBrowser extends AbstractWindow {

  def CardBrowser(IFrame iFrame) {
    super(iFrame);
  }

    public void init(Map<String, Object> params) {
    super.init(params);
    Table table = getComponent("cardTable")
    TableActionsHelper helper = new TableActionsHelper(this, table)
    helper.createRefreshAction()
    helper.createCreateAction()
    table.addAction(new ActionAdapter('open', [
            actionPerform: {
              Set selected = table.getSelected()
              if (selected.size() == 1) {
                Window window = openEditor('wf$Card.edit', selected.iterator().next(), WindowManager.OpenType.THIS_TAB)
                window.addListener({String actionId -> 
                  if (actionId == Window.COMMIT_ACTION_ID) {
                    table.getDatasource().refresh()
                  }
                } as Window.CloseListener)
              }
            },
            getCaption: {
              return getMessage('open')
            }
    ]))
    helper.createRemoveAction()
  }
}