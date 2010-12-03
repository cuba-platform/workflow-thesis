/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 11:20:55
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.web.ui.base.AbstractWfAccessData;

import java.util.List;
import java.util.ArrayList;

public class ActionsFrame extends AbstractFrame {

    private AssignmentInfo info;

    private BoxLayout buttonContainer;

    public ActionsFrame(IFrame frame) {
        super(frame);
    }

    public AssignmentInfo getInfo() {
        return info;
    }

    public void initActions(Card card, boolean commentVisible) {
        buttonContainer =  getComponent("buttonContainer");
        List<Button> buttons = new ArrayList<Button>();
        for (int i = 0; i < 7; i++) {
            Button btn = getComponent("actionBtn" + i);
            btn.setVisible(false);
            buttons.add(btn);
            buttonContainer.remove(btn);
        }

        TextField descrText = getComponent("descrText");

        List<String> actions = new ArrayList<String>();

        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        if (accessData == null || accessData.getSaveEnabled()) {
            actions.add(WfConstants.ACTION_SAVE);
        }

        if (accessData == null || accessData.getSaveAndCloseEnabled()) {
            actions.add(WfConstants.ACTION_SAVE_AND_CLOSE);
        }

        if (card.getJbpmProcessId() != null) {
            WfService wfs = ServiceLocator.lookup(WfService.NAME);
            info = wfs.getAssignmentInfo(card);
            if (info != null) {
                descrText.setVisible(true);
                if (info.getDescription() != null)
                    descrText.setValue(MessageUtils.loadString(card.getProc().getMessagesPack(), info.getDescription()));
                descrText.setEditable(false);

                actions.addAll(info.getActions());
            }
            if (!WfUtils.isCardInState(card,  WfConstants.CARD_STATE_CANCELED) && (accessData == null || accessData.getCancelProcessEnabled())) {
                actions.add(WfConstants.ACTION_CANCEL);
            }
        } else if (card.getProc() != null && card.getJbpmProcessId() == null &&
                (accessData == null || accessData.getStartProcessEnabled())) {
            actions.add(WfConstants.ACTION_START);
        }

        List<String> visibleActions = (accessData == null) ? null : accessData.getVisibleActions(card);

        if (visibleActions != null)
            actions.retainAll(visibleActions);
        for (int i = 0; i < buttons.size(); i++) {
            Button btn = buttons.get(i);
            if (i <= actions.size() - 1) {
                btn.setVisible(true);
                btn.setAction(new ProcessAction(card, actions.get(i), this));
                buttonContainer.add(btn);
            }
        }
    }

    public void addButton(Button button) {
        if (button == null) {
            return;
        }

        buttonContainer.add(button);
    }

    public void removeButton(Button button) {
        if (button == null) {
            return;
        }

        buttonContainer.remove(button);
    }
}
