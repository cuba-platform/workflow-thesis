/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 11:22:05
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.core.global.WfService;

public class ProcessAction extends AbstractAction {

    private Card card;
    private String actionName;
    private ActionsFrame frame;

    protected ProcessAction(Card card, String actionName, ActionsFrame frame) {
        super(actionName);
        this.card = card;
        this.actionName = actionName;
        this.frame = frame;
    }

    public String getCaption() {
        if (card.getProc() != null)
            return MessageProvider.getMessage(card.getProc().getMessagesPack(), getId());
        else
            return "";
    }

    public void actionPerform(Component component) {
        Window window = ComponentsHelper.getWindow(frame);
        if (window instanceof Window.Editor)
            ((Window.Editor) window).commit();

        WfService wfs = ServiceLocator.lookup(WfService.JNDI_NAME);
        if (WfConstants.ACTION_SAVE.equals(actionName)) {

        } else if (WfConstants.ACTION_START.equals(actionName)) {
            wfs.startProcess(card);

        } else {
            String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
            wfs.finishAssignment(frame.getInfo().getAssignmentId(), outcome, (String) frame.getCommentText().getValue());
        }

        window.close(Window.COMMIT_ACTION_ID);
    }
}
