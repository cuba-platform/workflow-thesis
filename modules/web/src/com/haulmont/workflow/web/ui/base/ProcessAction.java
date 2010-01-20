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
import com.haulmont.workflow.core.app.WfService;

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
        final Window window = ComponentsHelper.getWindow(frame);
        if (window instanceof Window.Editor && ((Window.Editor) window).commit()) {

            if (WfConstants.ACTION_SAVE.equals(actionName)) {
                window.close(Window.COMMIT_ACTION_ID);

            } else if (WfConstants.ACTION_START.equals(actionName)) {
                WfService wfs = ServiceLocator.lookup(WfService.NAME);
                wfs.startProcess(card);
                window.close(Window.COMMIT_ACTION_ID);

            } else {
                FormManager formManager = FormManager.create(
                        card,
                        frame.getInfo(),
                        (String) frame.getCommentText().getValue(),
                        actionName
                );
                if (formManager != null) {
                    final Window screen = formManager.show();
                    screen.addListener(new Window.CloseListener() {
                        public void windowClosed(String actionId) {
                            if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                                String comment = screen instanceof AbstractForm ?
                                        ((AbstractForm) screen).getComment() :
                                        (String) frame.getCommentText().getValue();
                                finishAssignment(comment);
                                window.close(Window.COMMIT_ACTION_ID);
                            }
                        }
                    });
                } else {
                    finishAssignment((String) frame.getCommentText().getValue());
                    window.close(Window.COMMIT_ACTION_ID);
                }
            }
        }
    }

    private void finishAssignment(String comment) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        wfs.finishAssignment(frame.getInfo().getAssignmentId(), outcome, comment);
    }
}
