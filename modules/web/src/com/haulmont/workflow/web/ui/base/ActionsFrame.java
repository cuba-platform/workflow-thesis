/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 11:20:55
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;

import java.util.List;
import java.util.ArrayList;

public class ActionsFrame extends AbstractFrame {

    private TextField commentText;
    private AssignmentInfo info;

    public ActionsFrame(IFrame frame) {
        super(frame);
    }

    public TextField getCommentText() {
        return commentText;
    }

    public AssignmentInfo getInfo() {
        return info;
    }

    public void initActions(Card card) {
        List<Button> buttons = new ArrayList<Button>();
        for (int i = 0; i < 5; i++) {
            Button btn = getComponent("actionBtn" + i);
            btn.setVisible(false);
            buttons.add(btn);
        }

        TextField descrText = getComponent("descrText");
        Label commentLab = getComponent("commentLab");
        commentText = getComponent("commentText");

        List<String> actions = new ArrayList<String>();

        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        if (accessData == null || accessData.getSaveEnabled()) {
            actions.add(WfConstants.ACTION_SAVE);
        }

        if (card.getJbpmProcessId() != null) {
            WfService wfs = ServiceLocator.lookup(WfService.JNDI_NAME);
            info = wfs.getAssignmentInfo(card);
            if (info != null) {
                descrText.setVisible(true);
                if (info.getDescription() != null)
                    descrText.setValue(MessageUtils.loadString(card.getProc().getMessagesPack(), info.getDescription()));
                descrText.setEditable(false);

                commentLab.setVisible(true);
                commentText.setVisible(true);

                actions.addAll(info.getActions());
            }
        } else if (card.getProc() != null && card.getJbpmProcessId() == null) {
            actions.add(WfConstants.ACTION_START);
        }

        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            if (i <= actions.size() - 1) {
                btn.setVisible(true);
                btn.setAction(new ProcessAction(card, actions.get(i), this));
            }
        }
    }
}
