/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 26.11.2009 18:05:52
 *
 * $Id$
 */
package workflow.client.web.ui.actions

import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.global.AssignmentInfo
import com.haulmont.workflow.core.global.WfConstants
import com.haulmont.workflow.core.global.WfService
import java.util.List
import workflow.client.web.ui.actions.ProcessAction
import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.core.global.MessageProvider
import com.haulmont.cuba.core.global.MessageUtils

public class ActionsFrame extends AbstractFrame {

  TextField commentText
  AssignmentInfo info
  
  def ActionsFrame(IFrame frame) {
    super(frame)
  }

  public void initActions(Card card) {
    List<Button> buttons = []
    for (i in 0..4) {
      buttons.add(getComponent("actionBtn$i"))
    }

    Label assignmentLab = getComponent("assignmentLab")
    TextField descrText = getComponent("descrText")
    Label commentLab = getComponent("commentLab")
    commentText = getComponent("commentText")


    List<String> actions
    if (card.jbpmProcessId) {
      WfService wfs = ServiceLocator.lookup(WfService.JNDI_NAME)
      info = wfs.getAssignmentInfo(card)
      if (info) {
        assignmentLab.setVisible(true)

        descrText.setVisible(true)
        descrText.setValue(MessageUtils.loadString(card.getProc().getMessagesPack(), info.getDescription()))
        descrText.setEditable(false)

        commentLab.setVisible(true) 
        commentText.setVisible(true)

        actions = info.getActions()
      }
    } else {
      actions = [WfConstants.ACTION_START]
    }

    buttons.eachWithIndex {Button btn, int idx ->
      if (actions) {
        if (idx <= actions.size()-1) {
          btn.setVisible(true)
          btn.setAction(new ProcessAction(card, actions[idx], this))
        }
      }
    }
  }
}
