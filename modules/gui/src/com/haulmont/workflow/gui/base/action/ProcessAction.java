/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.base.action;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.BooleanUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Action to be used in card editor screens to display available outcomes from the current card's state for the
 * current user.
 *
 */
public class ProcessAction extends AbstractAction {

    protected Card card;
    protected String actionName;
    protected AssignmentInfo assignmentInfo;
    protected Window frame;

    protected Window.Editor editor;

    protected Messages messages = AppBeans.get(Messages.NAME);
    protected WfService wfService = AppBeans.get(WfService.NAME);
    protected DataService dataService = AppBeans.get(DataService.NAME);
    protected Metadata metadata = AppBeans.get(Metadata.NAME);
    protected WindowManagerProvider wmp = AppBeans.get(WindowManagerProvider.NAME);

    public ProcessAction(Card card, String actionName, AssignmentInfo assignmentInfo, Window frame) {
        super(actionName);
        this.card = card;
        this.actionName = actionName;
        this.assignmentInfo = assignmentInfo;
        this.frame = frame;
    }

    @Override
    public String getCaption() {
        if (assignmentInfo != null && !card.equals(assignmentInfo.getCard()) && assignmentInfo.getCard() != null) {
            if (assignmentInfo.getCard().getProc() != null)
                return messages.getMessage(assignmentInfo.getCard().getProc().getMessagesPack(), getId());
        }
        if (card.getProc() != null) {
            if (card.getProc().getMessagesPack() == null)
                throw new DevelopmentException("Proc.messagePack is null. " +
                        "Make sure you load the card with the view containing this attrbute.");
            return messages.getMessage(card.getProc().getMessagesPack(), getId());
        } else
            return messages.getMessage(AppConfig.getMessagesPack(), getId());
    }

    protected FormManagerChain createManagerChain() {
        final Window window = ComponentsHelper.getWindow(frame);
        card = (Card) ((Window.Editor) window).getItem();

        final UUID assignmentId = assignmentInfo == null ? null : assignmentInfo.getAssignmentId();
        Card currentCard;
        if (assignmentInfo == null || assignmentInfo.getCard() == null || card.equals(assignmentInfo.getCard()))
            currentCard = card;
        else
            currentCard = assignmentInfo.getCard();
        final FormManagerChain managerChain = FormManagerChain.getManagerChain(currentCard, actionName);
        managerChain.setCard(card);
        managerChain.getCommonParams().put("procContextCard", currentCard);
        managerChain.setAssignmentId(assignmentId);
        return managerChain;
    }

    @Override
    public void actionPerform(Component component) {
        final Window window = ComponentsHelper.getWindow(frame);
        if (!(window instanceof Window.Editor)) return;

        editor = (Window.Editor) window;

        final Map<String, Object> formManagerParams = new HashMap<>();

        formManagerParams.put("subProcCard", new CardContext());

        card = (Card) editor.getItem();

        if (isCardDeleted(card)) {
            wmp.get().showNotification(
                    messages.getMessage(ProcessAction.class, "cardWasDeletedByAnotherUser"),
                    Frame.NotificationType.WARNING
            );
            editor.close(Window.CLOSE_ACTION_ID, true);
            return;
        }

        //we won't commit the editor if user presses no in cancel process confirmation form
        if (WfConstants.ACTION_CANCEL.equals(actionName)) {
            handleActionCancel(formManagerParams);
        } else if (commitEditor(editor)) {

            card = (Card) editor.getItem();
            checkVersion(card);

            final FormManagerChain managerChain = createManagerChain();

            switch (actionName) {
                case WfConstants.ACTION_SAVE:
                    handleActionSave(managerChain, formManagerParams);
                    break;
                case WfConstants.ACTION_SAVE_AND_CLOSE:
                    handleActionSaveAndClose(managerChain, formManagerParams);
                    break;
                case WfConstants.ACTION_START:
                    handleActionStart(managerChain, formManagerParams);
                    break;
                default:
                    handleFallback(managerChain, formManagerParams);
                    break;
            }
        }
    }

    /**
     * Checks if card opened in editor differs with the card in the database
     * Check version is performed after editor.commit() call to verify that
     * card was not modified.
     * P.S. If datasource was not modified, editor.commit() will not make real commit.
     * <p/>
     * If card was modified, optimistic lock exception is thrown
     */
    protected boolean checkVersion(Card card) {
        Preconditions.checkArgument(!PersistenceHelper.isNew(card), "Card can not be new");

        LoadContext<Card> lc = new LoadContext<>(Card.class);
        lc.setId(card.getId());
        Card reloadedCard = dataService.load(lc);

        /* If card was modified, initiate Optimistic Lock */
        if (reloadedCard.getVersion().compareTo(card.getVersion()) != 0) {
            dataService.commit(new CommitContext(card));
        }

        return true;
    }

    protected boolean commitEditor(Window.Editor editor) {
        return editor.commit();
    }

    protected void startProcess(Window window, FormManagerChain managerChain) {
        wfService.startProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();
    }

    protected void finishAssignment(Window window, String comment, FormManagerChain managerChain, Card subProcCard) {
        String outcome = actionName.substring(actionName.lastIndexOf('.') + 1);
        callFinishAssignment(comment, subProcCard, outcome, managerChain);
        window.close(Window.COMMIT_ACTION_ID, true);

        managerChain.doManagerAfter();

        afterFinish();
    }

    @SuppressWarnings("unused")
    protected void callFinishAssignment(String comment, Card subProcCard, String outcome,
                                        FormManagerChain managerChain) {
        wfService.finishAssignment(assignmentInfo.getAssignmentId(), outcome, comment, subProcCard);
    }

    protected void afterFinish() {
    }

    protected void removeSubProcCard(Card card) {
        if (card != null) {
            wfService.removeSubProcCard(card);
        }
    }

    protected void cancelProcess(Window window, FormManagerChain managerChain) {
        wfService.cancelProcess(card);
        window.close(Window.COMMIT_ACTION_ID, true);
        managerChain.doManagerAfter();
    }

    protected boolean isCardDeleted(Card card) {
        if (PersistenceHelper.isNew(card)) {
            return false;
        }
        Card reloadedCard = getReloadedCard(card);
        return reloadedCard == null;
    }

    protected Card getReloadedCard(Card card) {
        return dataService.load(new LoadContext<>(Card.class).setId(card.getId()).setView(View.LOCAL));
    }

    /**
     * Notice that next two methods are called in #actionPerform() after method #isCardDeleted()
     * That's why they can't throw NullPointerException
     * If you change this logic, handle the exception.
     */
    protected boolean isCardInProcess(Card card) {
        Card reloadedCard = dataService.load(new LoadContext<>(Card.class).setId(card.getId()).setView(View.LOCAL));
        return (reloadedCard.getJbpmProcessId() != null);
    }

    protected boolean isCardInSameProcess(Card card) {
        View withProcess = metadata.getViewRepository().getView(Card.class, "w-card-proc");
        Card reloadedCard = dataService.load(new LoadContext<>(Card.class).setId(card.getId()).setView(withProcess));

        if (CollectionUtils.isEmpty(card.getProcs())) {
            return true;
        }

        CardProc cardCP = null;
        for (CardProc cp : card.getProcs()) {
            if (BooleanUtils.isTrue(cp.getActive())) {
                cardCP = cp;
            }
        }

        CardProc reloadedCardCP = null;
        if (cardCP != null && reloadedCard != null && CollectionUtils.isNotEmpty(reloadedCard.getProcs())) {
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

    protected void handleActionCancel(final Map<String, Object> formManagerParams) {
        if (isCardInProcess(card) && isCardInSameProcess(card)) {
            wmp.get().showOptionDialog(
                    messages.getMessage(getClass(), "cancelProcess.title"),
                    getCancelProcessMessage(),
                    Frame.MessageType.CONFIRMATION,
                    new Action[]{
                            new DialogAction(Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    if (commitEditor(editor)) {

                                        card = (Card) editor.getItem();
                                        checkVersion(card);

                                        final FormManagerChain managerChain = createManagerChain();
                                        managerChain.setHandler(new FormManagerChain.Handler() {
                                            @Override
                                            public void onSuccess(String comment) {
                                                cancelProcess(editor, managerChain);
                                            }

                                            @Override
                                            public void onFail() {
                                            }
                                        });
                                        managerChain.doManagerBefore("", formManagerParams);
                                    }
                                }
                            },
                            new DialogAction(Type.NO, Status.PRIMARY)
                    }
            );
        } else {
            editor.showOptionDialog(
                    messages.getMessage(getClass(), "failCancelProcCaption"),
                    messages.getMessage(getClass(), "failCancelProcDescription"),
                    Frame.MessageType.CONFIRMATION,
                    new Action[]{
                            new DialogAction(Type.OK) {
                                @Override
                                public void actionPerform(Component c) {
                                    editor.close(Window.CLOSE_ACTION_ID, true);
                                }
                            },
                            new DialogAction(Type.NO, Status.PRIMARY)
                    }
            );
        }
    }

    protected String getCancelProcessMessage() {
        return messages.formatMessage(getClass(), "cancelProcess.message", card.getProc().getName());
    }

    protected void handleActionSave(final FormManagerChain managerChain, final Map<String, Object> formManagerParams) {
        managerChain.setHandler(new FormManagerChain.Handler() {
            @Override
            public void onSuccess(String comment) {
                managerChain.doManagerAfter(formManagerParams);
            }

            @Override
            public void onFail() {
            }
        });
        managerChain.doManagerBefore("", formManagerParams);
    }

    protected void handleActionSaveAndClose(final FormManagerChain managerChain, final Map<String, Object> formManagerParams) {
        managerChain.setHandler(new FormManagerChain.Handler() {
            @Override
            public void onSuccess(String comment) {
                editor.close(Window.COMMIT_ACTION_ID);
                managerChain.doManagerAfter(formManagerParams);
            }

            @Override
            public void onFail() {
            }
        });
        managerChain.doManagerBefore("", formManagerParams);
    }

    protected void handleActionStart(final FormManagerChain managerChain, final Map<String, Object> formManagerParams) {
        if (isCardInProcess(card)) {
            String msg = messages.getMainMessage("assignmentAlreadyFinished.message");
            wmp.get().showNotification(msg, Frame.NotificationType.ERROR);
            return;
        }

        managerChain.setHandler(new FormManagerChain.Handler() {
            @Override
            public void onSuccess(String comment) {
                startProcess(editor, managerChain);
            }

            @Override
            public void onFail() {
            }
        });
        managerChain.doManagerBefore("", formManagerParams);
    }

    protected void handleFallback(final FormManagerChain managerChain, final Map<String, Object> formManagerParams) {
        UUID assignmentId = assignmentInfo == null ? null : assignmentInfo.getAssignmentId();
        Assignment assignment = dataService.load(new LoadContext<>(Assignment.class).setId(assignmentId).setView(View.LOCAL));
        if (assignment.getFinished() != null) {
            String msg = messages.getMainMessage("assignmentAlreadyFinished.message");
            wmp.get().showNotification(msg, Frame.NotificationType.ERROR);
            return;
        }

        managerChain.setHandler(new FormManagerChain.Handler() {
            @Override
            public void onSuccess(String comment) {
                CardContext subProcCardContext = (CardContext) formManagerParams.get("subProcCard");
                finishAssignment(editor, comment, managerChain, subProcCardContext.getCard());
            }

            @Override
            public void onFail() {
                CardContext subProcCardContext = (CardContext) formManagerParams.get("subProcCard");
                removeSubProcCard(subProcCardContext.getCard());
            }
        });
        managerChain.doManagerBefore(assignment.getComment(), formManagerParams);
    }
}