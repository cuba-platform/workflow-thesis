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

import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Assignment;
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
            return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), getId());
    }

    public void actionPerform(Component component) {
        final Window window = ComponentsHelper.getWindow(frame);
        if (window instanceof Window.Editor && ((Window.Editor) window).commit()) {

            final UUID assignmentId = frame.getInfo() == null ? null : frame.getInfo().getAssignmentId();

            final FormManagerChain managerChain = FormManagerChain.getManagerChain(card, actionName);
            managerChain.setCard(card);
            managerChain.setAssignmentId(assignmentId);

            if (WfConstants.ACTION_SAVE.equals(actionName)) {
                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        if (window instanceof Window.Editor)
                            ((Window.Editor) window).commit();
                        else
                            throw new UnsupportedOperationException();
                        managerChain.doManagerAfter();
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("");

            } else if (WfConstants.ACTION_SAVE_AND_CLOSE.equals(actionName)) {
                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        window.close(Window.COMMIT_ACTION_ID);
                        managerChain.doManagerAfter();
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("");

            } else if (WfConstants.ACTION_START.equals(actionName)) {
                LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(View.LOCAL);
                Card loadedCard = ServiceLocator.getDataService().load(lc);
                if (loadedCard.getJbpmProcessId() != null) {
                    String msg = MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), "assignmentAlreadyFinished.message");
                    App.getInstance().getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
                    return;
                }

                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        startProcess(window, managerChain);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("");

            } else if (WfConstants.ACTION_CANCEL.equals(actionName)) {
                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        cancelProcess(window, managerChain);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("");

            } else {
                LoadContext lc = new LoadContext(Assignment.class).setId(assignmentId).setView(View.LOCAL);
                Assignment assignment = ServiceLocator.getDataService().load(lc);
                if (assignment.getFinished() != null) {
                    String msg = MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), "assignmentAlreadyFinished.message");
                    App.getInstance().getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
                    return;
                }

                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        finishAssignment(window, comment, managerChain);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("");
            }
        }
    }

    private void startProcess(Window window, FormManagerChain managerChain) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        wfs.startProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();
    }

    private void finishAssignment(Window window, String comment, FormManagerChain managerChain) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        wfs.finishAssignment(frame.getInfo().getAssignmentId(), outcome, comment);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();
    }

    private void cancelProcess(Window window, FormManagerChain managerChain) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        wfs.cancelProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);
        managerChain.doManagerAfter();
    }
    
}
