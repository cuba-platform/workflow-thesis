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

import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProcessAction extends AbstractAction {
    public static String SEND_PREFIX = "send_";

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
        AssignmentInfo assignmentInfo = frame.getInfo();
        if (assignmentInfo != null && !card.equals(frame.getInfo().getCard()) && frame.getInfo().getCard() != null) {
            if (frame.getInfo().getCard().getProc() != null)
                return MessageProvider.getMessage(frame.getInfo().getCard().getProc().getMessagesPack(), getId());
        }
        if (card.getProc() != null)
            return MessageProvider.getMessage(card.getProc().getMessagesPack(), getId());
        else
            return MessageProvider.getMessage(AppConfig.getMessagesPack(), getId());
    }

    public void actionPerform(Component component) {
        final Window window = ComponentsHelper.getWindow(frame);
        if (!(window instanceof Window.Editor)) return;

        card = (Card) ((Window.Editor) window).getItem();
        final UUID assignmentId = frame.getInfo() == null ? null : frame.getInfo().getAssignmentId();
        Card currentCard = null;
        if (frame.getInfo() == null || frame.getInfo().getCard() == null || card.equals(frame.getInfo().getCard()))
            currentCard = card;
        else
            currentCard = frame.getInfo().getCard();
        final FormManagerChain managerChain = FormManagerChain.getManagerChain(currentCard, actionName);
        managerChain.setCard(currentCard);
        managerChain.setAssignmentId(assignmentId);

        final Map<String, Object> formManagerParams = new HashMap<String, Object>();

        DsContext dsContext = window.getDsContext();
        if (dsContext != null)
            formManagerParams.put("modifed", dsContext.isModified());

        for (Object o : window.getContext().getParams().entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            String key = (String) entry.getKey();
            if (key.startsWith(SEND_PREFIX)) {
                formManagerParams.put(key.substring(5), entry.getValue());
            }
        }

        formManagerParams.put("subProcCard", new CardContext());

        //we won't commit the editor if user presses no in cancel process confirmation form
        if (WfConstants.ACTION_CANCEL.equals(actionName)) {
            App.getInstance().getWindowManager().showOptionDialog(
                    MessageProvider.getMessage(getClass(), "cancelProcess.title"),
                    MessageProvider.formatMessage(getClass(), "cancelProcess.message", card.getProc().getName()),
                    IFrame.MessageType.CONFIRMATION,
                    new Action[]{
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    if (((Window.Editor) window).commit()) {
                                        managerChain.setHandler(new FormManagerChain.Handler() {
                                            public void onSuccess(String comment) {
                                                cancelProcess(window, managerChain);
                                            }

                                            public void onFail() {
                                            }
                                        });
                                        managerChain.doManagerBefore("", formManagerParams);
                                    }
                                }
                            },
                            new DialogAction(DialogAction.Type.NO)
                    }
            );
        } else if ((window instanceof WebWindow) ? ((Window.Editor) ((WebWindow.Editor) window).getWrapper()).commit()
                : ((Window.Editor) window).commit()) {

            if (WfConstants.ACTION_SAVE.equals(actionName)) {
                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        if (window instanceof Window.Editor)
                            ((Window.Editor) window).commit();
                        else
                            throw new UnsupportedOperationException();
                        managerChain.doManagerAfter(formManagerParams);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("", formManagerParams);

            } else if (WfConstants.ACTION_SAVE_AND_CLOSE.equals(actionName)) {
                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        if (window instanceof WebWindow) {
                            ((WebWindow) window).getWrapper().close(Window.COMMIT_ACTION_ID);
                        } else {
                            window.close(Window.COMMIT_ACTION_ID);
                        }
                        managerChain.doManagerAfter(formManagerParams);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("", formManagerParams);

            } else if (WfConstants.ACTION_START.equals(actionName)) {
                LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(View.LOCAL);
                Card loadedCard = ServiceLocator.getDataService().load(lc);
                if (loadedCard.getJbpmProcessId() != null) {
                    String msg = AppBeans.get(Messages.class).getMainMessage("assignmentAlreadyFinished.message");
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
                managerChain.doManagerBefore("", formManagerParams);

            } else {
                LoadContext lc = new LoadContext(Assignment.class).setId(assignmentId).setView(View.LOCAL);
                Assignment assignment = ServiceLocator.getDataService().load(lc);
                if (assignment.getFinished() != null) {
                    String msg = AppBeans.get(Messages.class).getMainMessage("assignmentAlreadyFinished.message");
                    App.getInstance().getWindowManager().showNotification(msg, IFrame.NotificationType.ERROR);
                    return;
                }

                managerChain.setHandler(new FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        CardContext subProcCardContext = (CardContext) formManagerParams.get("subProcCard");
                        finishAssignment(window, comment, managerChain, subProcCardContext.getCard());
                    }

                    public void onFail() {
                        CardContext subProcCardContext = (CardContext) formManagerParams.get("subProcCard");
                        removeSubProcCard(subProcCardContext.getCard());
                    }
                });
                managerChain.doManagerBefore(assignment.getComment(), formManagerParams);
            }
        }
    }


    private void startProcess(Window window, FormManagerChain managerChain) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        wfs.startProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();
    }

    private void finishAssignment(Window window, String comment, FormManagerChain managerChain, Card subProcCard) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        wfs.finishAssignment(frame.getInfo().getAssignmentId(), outcome, comment, subProcCard);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();

        afterFinish();
    }

    protected void afterFinish() {
    }

    private void removeSubProcCard(Card card) {
        if (card != null) {
            WfService wfs = ServiceLocator.lookup(WfService.NAME);
            wfs.removeSubProcCard(card);
        }
    }

    private void cancelProcess(Window window, FormManagerChain managerChain) {
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        wfs.cancelProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);
        managerChain.doManagerAfter();
    }

}
