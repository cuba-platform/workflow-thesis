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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.AbstractWfAccessData;

import java.util.ArrayList;
import java.util.List;

public class ActionsFrame extends AbstractFrame {

    private AssignmentInfo info;

    public ActionsFrame(IFrame frame) {
        super(frame);
    }

    public AssignmentInfo getInfo() {
        return info;
    }

    public void initActions(Card card, boolean descriptionVisible) {
        TextField descrText = getComponent("descrText");

        deleteActionButtons();

        List<String> actions = new ArrayList<String>();

        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        if (accessData == null || accessData.getSaveAndCloseEnabled()) {
            actions.add(WfConstants.ACTION_SAVE_AND_CLOSE);
        }

        if (accessData == null || accessData.getSaveEnabled()) {
            actions.add(WfConstants.ACTION_SAVE);
        }


        if (card.getJbpmProcessId() != null) {
            if (accessData != null && accessData.getAssignmentInfo() != null) {
                info = accessData.getAssignmentInfo();
            } else {
                WfService wfs = ServiceLocator.lookup(WfService.NAME);
                info = wfs.getAssignmentInfo(card);
            }
            if (info != null) {
                if (descriptionVisible) {
                    descrText.setVisible(true);
                    descrText.setWidth("100%");
                    if (info.getDescription() != null)
                        descrText.setValue(AppBeans.get(MessageTools.class).loadString(card.getProc().getMessagesPack(), info.getDescription()));
                    descrText.setEditable(false);
                }

                actions.addAll(info.getActions());
            }
            if (!WfUtils.isCardInState(card, WfConstants.CARD_STATE_CANCELED) && (accessData == null || accessData.getCancelProcessEnabled())) {
                actions.add(WfConstants.ACTION_CANCEL);
            }
        } else if (card.getProc() != null && card.getJbpmProcessId() == null &&
                (accessData == null || accessData.getStartProcessEnabled())) {
            actions.add(WfConstants.ACTION_START);
        }

        List<String> visibleActions = (accessData == null) ? null : accessData.getVisibleActions(card);

        List<String> enabledActions = (accessData == null) ? null : accessData.getEnabledActions(card);

        if (visibleActions != null)
            actions.retainAll(visibleActions);

        for (String actionName : actions) {
            Button button = AppConfig.getFactory().createComponent(Button.NAME);
            button.setAction(new ProcessAction(card, actionName, this));
            button.setWidth("100%");

            if ((enabledActions != null) && !enabledActions.contains(actionName))
                button.setEnabled(false);
            FormManagerChain managerChain = null;
            if (info != null && info.getCard() != null && !card.equals(info.getCard()))
                managerChain = FormManagerChain.getManagerChain(info.getCard(), actionName);
            else
                managerChain = FormManagerChain.getManagerChain(card, actionName);
            String style = (String) managerChain.getCommonParams().get("style");
            if (style != null) {
                button.setStyleName(style);
            }

            add(button);
        }

    }

    private void deleteActionButtons() {
        for (Component component : getComponents()) {
            if (component instanceof Button) {
                remove(component);
            }
        }
    }

    public void addButton(Component button) {
        if (button == null) {
            return;
        }
        add(button);
    }

    public void removeButton(Component button) {
        if (button == null) {
            return;
        }
        remove(button);
    }
}
