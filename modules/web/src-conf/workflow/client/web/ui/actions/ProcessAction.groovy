/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.11.2009 19:05:30
 *
 * $Id$
 */
package workflow.client.web.ui.actions

import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.gui.AppConfig
import com.haulmont.cuba.gui.ComponentsHelper
import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.cuba.gui.components.AbstractAction
import com.haulmont.cuba.gui.components.Component
import com.haulmont.cuba.gui.components.Window
import com.haulmont.cuba.gui.components.Window.Editor
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.global.WfConstants
import com.haulmont.workflow.core.global.WfService
import workflow.client.web.ui.actions.ActionsFrame
import com.haulmont.cuba.gui.components.AbstractFrame

public class ProcessAction extends AbstractAction {

  private Card card
  private String actionName
  private ActionsFrame frame

  def ProcessAction(Card card, String actionName, AbstractFrame frame) {
    super(card.getProc().getJbpmProcessKey() + '.' + actionName)
    this.card = card
    this.actionName = actionName
    this.frame = frame
  }

  public String getCaption() {
    return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), id)
  }

  public void actionPerform(Component component) {
    Window window = ComponentsHelper.getWindow(frame)
    if (window instanceof Window.Editor)
      ((Window.Editor)window).commitAndClose()

    WfService wfs = ServiceLocator.lookup(WfService.JNDI_NAME)
    if (WfConstants.ACTION_START == actionName) {
      wfs.startProcess(card)
    } else {
      String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
      wfs.finishAssignment(frame.info.getAssignmentId(), outcome, frame.commentText.getValue())
    }
  }
}