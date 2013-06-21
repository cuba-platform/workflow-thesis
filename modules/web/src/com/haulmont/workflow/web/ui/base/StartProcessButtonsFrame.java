/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.web.gui.components.WebButton;
import com.haulmont.cuba.web.gui.components.WebVBoxLayout;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.gui.base.AbstractWfAccessData;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class StartProcessButtonsFrame extends AbstractFrame {
    @Inject
    protected CollectionDatasource<Proc, UUID> procDs;

    @Inject
    protected CardProcFrame cardProcFrame;

    @Named("cardProcFrame.procDs")
    protected CollectionDatasource<CardProc, UUID> cardProcDs;

    @Inject
    protected Datasource cardDs;

    protected List<Action> actions = new ArrayList<>();
    protected List<String> excludedProcessesCodes = new ArrayList<>();

    @Inject
    protected Messages messages;

    public void init() {
        if (isStartProcessEnabled() && !isActiveProcess()) {
            WebVBoxLayout startButtonsPanel = getComponent("startButtonsPanel");
            for (UUID uuid : procDs.getItemIds()) {
                final Proc proc = procDs.getItem(uuid);
                if (!excludedProcessesCodes.contains(proc.getCode())) {
                    Button button = new WebButton();

                    button.setWidth("100%");
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
        for (UUID uuid : cardProcDs.getItemIds()) {
            CardProc cpt = cardProcDs.getItem(uuid);
            if (cpt.getActive()) {
                return true;
            }
        }
        return false;
    }

    public void setExcludedProcesses(List<String> excludedProcessesCodes) {
        this.excludedProcessesCodes = excludedProcessesCodes;
    }

    public boolean isProcessesAvailable() {
        return (procDs != null && procDs.getItemIds().size() != 0);
    }

    private void startProcess(final Proc proc) {
        showOptionDialog(
                messages.getMessage(cardProcFrame.getClass(), "runProc.title"),
                String.format(messages.getMessage(cardProcFrame.getClass(), "runProc.msg"), proc.getName()),
                IFrame.MessageType.CONFIRMATION,
                new Action[]{
                        new DialogAction(DialogAction.Type.YES) {
                            @Override
                            public void actionPerform(Component component) {
                                CardProc cp = null;
                                for (UUID uuid : cardProcDs.getItemIds()) {
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
