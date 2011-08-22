/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebVBoxLayout;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.entity.Proc;

import java.util.*;

/**
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class StartProcessButtonsFrame extends AbstractFrame {
    private CollectionDatasource<Proc, UUID> procDs;
    private CardProcFrame cardProcFrame;
    private CollectionDatasource<CardProc, UUID> cardProcDs;
    private Datasource cardDs;
    private java.util.List<Action> actions = new ArrayList<Action>();

    public StartProcessButtonsFrame(IFrame frame) {
        super(frame);
    }

    public void init() {

        cardProcFrame = getComponent("cardProcFrame");
        cardDs = getDsContext().get("cardDs");

        final CardProcFrame cardProcFrame = (CardProcFrame) getComponent("cardProcFrame");
        procDs = cardProcFrame.getDsContext().get("procDs");

        cardProcDs = getDsContext().get("cardProcDs");

        if (isStartProcessEnabled() && !isActiveProcess()) {
            WebVBoxLayout startButtonsPanel = getComponent("startButtonsPanel");
            for (UUID uuid : procDs.getItemIds()) {
                final Proc proc = (Proc) procDs.getItem(uuid);

                Button button = new WebButton();

                button.setWidth("200px");
                AbstractAction buttonAction = new AbstractAction("start" + proc.getJbpmProcessKey()) {
                    public void actionPerform(Component component) {
                        startProcess(proc);
                    }

                    @Override
                    public String getCaption() {
                        return proc.getName();
                    }
                };
                actions.add(buttonAction);
                button.setAction(buttonAction);
                startButtonsPanel.add(button);
            }

        }
    }

    public Action getAction(String id) {
        for (Action action : actions) {
            if (action.getId().equals(id))
                return action;
        }
        return null;
    }

    public java.util.List<Action> getActions() {
        return actions;
    }

    public boolean isStartProcessEnabled() {
        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        return (accessData != null && accessData.getStartCardProcessEnabled());
    }

    public boolean isActiveProcess() {
        for (UUID uuid : (Collection<UUID>) cardProcDs.getItemIds()) {
            CardProc cpt = (CardProc) cardProcDs.getItem(uuid);
            if (cpt.getActive()) {
                return true;
            }
        }
        return false;
    }

    public boolean isProcessesAvailable() {
        return (procDs != null && procDs.getItemIds().size() != 0);
    }

    private void startProcess(final Proc proc) {
        showOptionDialog(
                MessageProvider.getMessage(cardProcFrame.getClass(), "runProc.title"),
                String.format(MessageProvider.getMessage(cardProcFrame.getClass(), "runProc.msg"), proc.getName()),
                IFrame.MessageType.CONFIRMATION,
                new Action[]{
                        new DialogAction(DialogAction.Type.YES) {
                            @Override
                            public void actionPerform(Component component) {
                                CardProc cp = null;
                                for (UUID uuid : (Collection<UUID>)cardProcDs.getItemIds()) {
                                    CardProc cpt = cardProcDs.getItem(uuid);
                                    if (cpt.getProc().getId().equals(proc.getId())) {
                                        cp = cpt;
                                    }
                                }
                                if (cp == null) {
                                    cp = new CardProc();
                                    cp.setCard((Card) cardDs.getItem());
                                    cp.setProc(proc);
                                    cp.setActive(false);
                                    cp.setSortOrder(cardProcFrame.calculateSortOrder());
                                    cardProcDs.addItem(cp);
                                    cardProcFrame.getCardRolesFrame().procChanged(cp.getProc());
                                    cardProcFrame.getCardRolesFrame().initDefaultActors(cp.getProc());
                                }
                                cardProcFrame.startProcess(cp);
                            }
                        },
                        new DialogAction(DialogAction.Type.NO)
                }
        );

    }
}
