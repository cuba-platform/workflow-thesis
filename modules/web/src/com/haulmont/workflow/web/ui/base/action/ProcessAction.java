/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 11:22:05
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;

import java.util.UUID;

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

            final UUID assignmentId = frame.getInfo() == null ? null : frame.getInfo().getAssignmentId();

            final FormManager before = FormManager.getManagerBefore(card, actionName);
            final FormManager after = FormManager.getManagerAfter(card, actionName);

            if (WfConstants.ACTION_SAVE.equals(actionName)) {
                if (before != null) {
                    before.doBefore(card, assignmentId, (String) frame.getCommentText().getValue(),
                            new FormManager.Handler() {
                                public void commit(String comment) {
                                    window.close(Window.COMMIT_ACTION_ID);
                                    if (after != null)
                                        after.doAfter(card, assignmentId);
                                }
                            }
                    );
                } else {
                    window.close(Window.COMMIT_ACTION_ID);
                    if (after != null)
                        after.doAfter(card, assignmentId);
                }

            } else if (WfConstants.ACTION_START.equals(actionName)) {
                if (before != null) {
                    before.doBefore(card, assignmentId, (String) frame.getCommentText().getValue(),
                            new FormManager.Handler() {
                                public void commit(String comment) {
                                    startProcess(window, after);
                                }
                            }
                    );
                } else {
                    startProcess(window, after);
                }

            } else {
                if (before != null) {
                    before.doBefore(card, assignmentId,(String) frame.getCommentText().getValue(),
                            new FormManager.Handler() {
                                public void commit(String comment) {
                                    finishAssignment(window, comment, after);
                                }
                            }
                    );
                } else {
                    finishAssignment(window, (String) frame.getCommentText().getValue(), after);
                }
            }
        }
    }

    private void startProcess(Window window, FormManager after) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        wfs.startProcess(card);
        window.close(Window.COMMIT_ACTION_ID);

        if (after != null)
            after.doAfter(card, null);
    }

    private void finishAssignment(Window window, String comment, FormManager after) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        wfs.finishAssignment(frame.getInfo().getAssignmentId(), outcome, comment);
        window.close(Window.COMMIT_ACTION_ID);

        if (after != null)
            after.doAfter(card, frame.getInfo().getAssignmentId());
    }
}
