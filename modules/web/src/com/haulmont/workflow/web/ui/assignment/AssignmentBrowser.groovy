/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.assignment

import com.haulmont.cuba.gui.components.*
import com.haulmont.workflow.core.entity.Card
import com.haulmont.cuba.gui.WindowManager

/**
 * @author krivopustov
 * @version $Id$
 */
public class AssignmentBrowser extends AbstractWindow {

    public void init(Map<String, Object> params) {
    super.init(params);
    Table table = getComponent("aTable")
    TableActionsHelper helper = new TableActionsHelper(this, table)
    helper.createRefreshAction()
    table.addAction(new ActionAdapter('open', [
            actionPerform: {
              Set selected = table.getSelected()
              if (selected.size() == 1) {
                Card card = selected.iterator().next().getCard()
                Window window = openEditor('wf$Card.edit', card, WindowManager.OpenType.THIS_TAB)
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
  }
}