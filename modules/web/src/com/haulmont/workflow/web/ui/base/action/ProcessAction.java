/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.base.action.CardContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ProcessAction extends AbstractAction {
    protected Card card;
    protected String actionName;
    protected ActionsFrame frame;

    protected Messages messages = AppBeans.get(Messages.NAME);
    protected WfService wfService = AppBeans.get(WfService.NAME);
    protected DataService dataService = AppBeans.get(DataService.NAME);
    protected Metadata metadata = AppBeans.get(Metadata.NAME);

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
                return messages.getMessage(frame.getInfo().getCard().getProc().getMessagesPack(), getId());
        }
        if (card.getProc() != null)
            return messages.getMessage(card.getProc().getMessagesPack(), getId());
        else
            return messages.getMessage(AppConfig.getMessagesPack(), getId());
    }

    public void actionPerform(Component component) {
        final Window window = ComponentsHelper.getWindow(frame);
        if (!(window instanceof Window.Editor)) return;

        card = (Card) ((Window.Editor) window).getItem();
        final UUID assignmentId = frame.getInfo() == null ? null : frame.getInfo().getAssignmentId();
        Card currentCard;
        if (frame.getInfo() == null || frame.getInfo().getCard() == null || card.equals(frame.getInfo().getCard()))
            currentCard = card;
        else
            currentCard = frame.getInfo().getCard();
        final com.haulmont.workflow.gui.base.action.FormManagerChain managerChain = com.haulmont.workflow.gui.base.action.FormManagerChain.getManagerChain(currentCard, actionName);
        managerChain.setCard(currentCard);
        managerChain.setAssignmentId(assignmentId);

        final Map<String, Object> formManagerParams = new HashMap<>();

        formManagerParams.put("subProcCard", new CardContext());

        WindowManagerProvider wmp = AppBeans.get(WindowManagerProvider.NAME);

        if (isCardDeleted(card)) {
            wmp.get().showNotification(
                    messages.getMessage(getClass(), "cardWasDeletedByAnotherUser"),
                    IFrame.NotificationType.WARNING
            );
            window.close(Window.CLOSE_ACTION_ID, true);
            return;
        }

        //we won't commit the editor if user presses no in cancel process confirmation form
        if (WfConstants.ACTION_CANCEL.equals(actionName)) {
            if (isCardInProcess(card) && isCardInSameProcess(card)) {
                wmp.get().showOptionDialog(
                        messages.getMessage(getClass(), "cancelProcess.title"),
                        messages.formatMessage(getClass(), "cancelProcess.message", card.getProc().getName()),
                        IFrame.MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(DialogAction.Type.YES) {
                                    @Override
                                    public void actionPerform(Component component) {
                                        if (((Window.Editor) window).commit()) {
                                            managerChain.setHandler(new com.haulmont.workflow.gui.base.action.FormManagerChain.Handler() {
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
            } else {
                window.showOptionDialog(
                        messages.getMessage(getClass(), "failCancelProcCaption"),
                        messages.getMessage(getClass(), "failCancelProcDescription"),
                        IFrame.MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(DialogAction.Type.OK) {
                                    @Override
                                    public void actionPerform(Component c) {
                                        window.close(Window.CLOSE_ACTION_ID, true);
                                    }
                                },
                                new DialogAction(DialogAction.Type.NO) {
                                    @Override
                                    public void actionPerform(Component c) {
                                    }
                                }
                        }
                );
            }
        } else if (((Window.Editor) window).commit()) {

            if (WfConstants.ACTION_SAVE.equals(actionName)) {
                managerChain.setHandler(new com.haulmont.workflow.gui.base.action.FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        ((Window.Editor) window).commit();
                        managerChain.doManagerAfter(formManagerParams);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("", formManagerParams);

            } else if (WfConstants.ACTION_SAVE_AND_CLOSE.equals(actionName)) {
                managerChain.setHandler(new com.haulmont.workflow.gui.base.action.FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        window.close(Window.COMMIT_ACTION_ID);
                        managerChain.doManagerAfter(formManagerParams);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("", formManagerParams);

            } else if (WfConstants.ACTION_START.equals(actionName)) {
                if (isCardInProcess(card)) {
                    String msg = messages.getMainMessage("assignmentAlreadyFinished.message");
                    wmp.get().showNotification(msg, IFrame.NotificationType.ERROR);
                    return;
                }

                managerChain.setHandler(new com.haulmont.workflow.gui.base.action.FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        startProcess(window, managerChain);
                    }

                    public void onFail() {
                    }
                });
                managerChain.doManagerBefore("", formManagerParams);

            } else {
                LoadContext lc = new LoadContext(Assignment.class).setId(assignmentId).setView(View.LOCAL);
                Assignment assignment = dataService.load(lc);
                if (assignment.getFinished() != null) {
                    String msg = messages.getMainMessage("assignmentAlreadyFinished.message");
                    wmp.get().showNotification(msg, IFrame.NotificationType.ERROR);
                    return;
                }

                managerChain.setHandler(new com.haulmont.workflow.gui.base.action.FormManagerChain.Handler() {
                    public void onSuccess(String comment) {
                        com.haulmont.workflow.gui.base.action.CardContext subProcCardContext = (com.haulmont.workflow.gui.base.action.CardContext) formManagerParams.get("subProcCard");
                        finishAssignment(window, comment, managerChain, subProcCardContext.getCard());
                    }

                    public void onFail() {
                        com.haulmont.workflow.gui.base.action.CardContext subProcCardContext = (com.haulmont.workflow.gui.base.action.CardContext) formManagerParams.get("subProcCard");
                        removeSubProcCard(subProcCardContext.getCard());
                    }
                });
                managerChain.doManagerBefore(assignment.getComment(), formManagerParams);
            }
        }
    }


    protected void startProcess(Window window, com.haulmont.workflow.gui.base.action.FormManagerChain managerChain) {
        wfService.startProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();
    }

    protected void finishAssignment(Window window, String comment, com.haulmont.workflow.gui.base.action.FormManagerChain managerChain, Card subProcCard) {
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        wfService.finishAssignment(frame.getInfo().getAssignmentId(), outcome, comment, subProcCard);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();

        afterFinish();
    }

    protected void afterFinish() {
    }

    protected void removeSubProcCard(Card card) {
        if (card != null) {
            wfService.removeSubProcCard(card);
        }
    }

    protected void cancelProcess(Window window, com.haulmont.workflow.gui.base.action.FormManagerChain managerChain) {
        wfService.cancelProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);
        managerChain.doManagerAfter();
    }

    protected boolean isCardDeleted(Card card) {
        if (PersistenceHelper.isNew(card)) {
            return false;
        }

        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(View.LOCAL);
        Card reloadedCard = dataService.load(lc);
        return (reloadedCard == null);
    }

    /**
     * Notice that next two methods are called in #actionPerform() after method #isCardDeleted()
     * That's why they can't throw NullPointerException
     * If you change this logic, handle the exception.
     */
    protected boolean isCardInProcess(Card card) {
        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(View.LOCAL);
        Card reloadedCard = dataService.load(lc);
        return (reloadedCard.getJbpmProcessId() != null);
    }

    protected boolean isCardInSameProcess(Card card) {
        View withProcess = metadata.getViewRepository().getView(Card.class, "w-card-proc");
        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(withProcess);
        Card reloadedCard = dataService.load(lc);

        if (card.getProcs() == null) {
            return true;
        }

        CardProc cardCP = !card.getProcs().isEmpty() ? card.getProcs().get(0) : null;
        CardProc reloadedCardCP = null;
        if (cardCP != null && reloadedCard != null && !reloadedCard.getProcs().isEmpty()) {
            for (CardProc cp : reloadedCard.getProcs()) {
                if (cp.getProc() != null && cardCP.getProc() != null &&
                        cp.getProc().equals(cardCP.getProc())) {
                    reloadedCardCP = cp;
                }
            }
        }

        return (cardCP != null && reloadedCardCP != null
                && cardCP.equals(reloadedCardCP)
                && cardCP.getStartCount().equals(reloadedCardCP.getStartCount()));
    }
}
