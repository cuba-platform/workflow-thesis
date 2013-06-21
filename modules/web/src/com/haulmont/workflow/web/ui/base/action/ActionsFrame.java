/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.base.AbstractWfAccessData;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ActionsFrame extends AbstractFrame {

    private AssignmentInfo info;

    @Inject
    protected TextArea descrText;

    @Inject
    protected WfService wfs;

    public AssignmentInfo getInfo() {
        return info;
    }

    public void initActions(Card card, boolean descriptionVisible) {
        deleteActionButtons();

        List<String> actions = new ArrayList<>();

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
            if (accessData != null && accessData.getReassignInfo() != null) {
                actions.add(WfConstants.ACTION_REASSIGN);
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
            button.setAction(createAction(card, actionName));
            button.setWidth("100%");

            if ((enabledActions != null) && !enabledActions.contains(actionName))
                button.setEnabled(false);
            com.haulmont.workflow.gui.base.action.FormManagerChain managerChain;
            if (info != null && info.getCard() != null && !card.equals(info.getCard()))
                managerChain = com.haulmont.workflow.gui.base.action.FormManagerChain.getManagerChain(info.getCard(), actionName);
            else
                managerChain = com.haulmont.workflow.gui.base.action.FormManagerChain.getManagerChain(card, actionName);
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

    protected Action createAction(Card card, String actionName) {
        if (WfConstants.ACTION_REASSIGN.equals(actionName))
            return new ReassignAction(card, this);
        else
            return new ProcessAction(card, actionName, this);
    }
}
