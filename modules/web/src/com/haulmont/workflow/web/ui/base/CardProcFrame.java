/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 10:11:47
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.log.LogItem;
import com.haulmont.cuba.web.log.LogLevel;
import com.haulmont.workflow.core.app.ProcRolePermissionsService;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardProc;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.FormManagerChain;
import org.apache.commons.lang.BooleanUtils;

import java.util.*;
import java.util.List;

import static com.haulmont.cuba.gui.WindowManager.OpenType;

public class CardProcFrame extends AbstractFrame {

    protected Card card;
    private boolean enabled = true;

    private CollectionDatasource<CardRole, UUID> cardRolesDs;
    private CollectionDatasource<Proc, UUID> procDs;
    protected CollectionDatasource<CardProc, UUID> cardProcDs;
    private LookupField createProcLookup;
    private Table cardProcTable;
    protected AbstractAction startProcessAction;
    private CardRolesFrame cardRolesFrame;

    private String createProcCaption;

    protected ProcRolePermissionsService procRolePermissionsService;

    public CardProcFrame(IFrame frame) {
        super(frame);
    }

    public void init() {
        cardProcDs = getDsContext().get("cardProcDs");
        Preconditions.checkState(cardProcDs != null, "Enclosing window must declare 'cardProcsDs' datasource");

        initProc();
        initRoles();

        procRolePermissionsService = getProcRolePermissionsService();
    }

    private void initProc() {
        createProcCaption = getMessage("createProcCaption");
        createProcLookup = getComponent("createProcLookup");

        procDs = getDsContext().get("procDs");
        cardRolesDs = getDsContext().get("cardRolesDs");

        cardProcTable = getComponent("cardProcTable");
        TableActionsHelper procsTH = new TableActionsHelper(this, cardProcTable);

        final Action removeAction = procsTH.createRemoveAction(false);
        removeAction.setEnabled(false);
        procsTH.addListener(new TableActionsHelper.Listener() {
            public void entityCreated(Entity entity) {
            }

            public void entityEdited(Entity entity) {
            }

            public void entityRemoved(Set<Entity> entity) {
                for (Entity e : entity) {
                    if (!(e instanceof CardProc))
                        continue;
                    CardProc cp = (CardProc) e;
                    for (UUID id : new ArrayList<UUID>(cardRolesDs.getItemIds())) {
                        CardRole cardRole = cardRolesDs.getItem(id);
                        if (cardRole.getProcRole().getProc().equals(cp.getProc())) {
                            cardRolesDs.removeItem(cardRole);
                        }
                    }
                }
            }
        });

        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        if (accessData == null || accessData.getStartCardProcessEnabled()) {
            startProcessAction = new StartProcessAction();
            startProcessAction.setEnabled(false);
            cardProcTable.addAction(startProcessAction);
            Button startProcBtn = getComponent("startProc");
            startProcBtn.setAction(startProcessAction);
            startProcBtn.setVisible(true);
        }

        final boolean removeActionEnabled = accessData == null || accessData.getRemoveCardProcessEnabled();

        cardProcDs.addListener(
                new CollectionDsListenerAdapter<CardProc>() {
                    @Override
                    public void itemChanged(Datasource<CardProc> ds, CardProc prevItem, CardProc item) {
                        cardRolesFrame.procChanged(item == null ? null : item.getProc());
                        cardRolesFrame.setCardProc(item);

                        boolean enabled = item != null && !BooleanUtils.isTrue(item.getActive());
                        removeAction.setEnabled(enabled && removeActionEnabled);

                        if (startProcessAction != null) {
                            if (enabled) {
                                for (UUID id : cardProcDs.getItemIds()) {
                                    if (BooleanUtils.isTrue(cardProcDs.getItem(id).getActive())) {
                                        enabled = false;
                                        break;
                                    }
                                }
                            }
                            startProcessAction.setEnabled(enabled);
                        }
                    }

                    @Override
                    public void collectionChanged(CollectionDatasource ds, CollectionDatasourceListener.Operation operation) {
                        initCreateProcLookup();
                    }

                }
        );

        createProcLookup.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                if ((value == null) || createProcCaption.equals(value))
                    return;

                CardProc cp = new CardProc();
                cp.setCard(card);
                cp.setProc((Proc) value);
                cp.setActive(false);
                cp.setSortOrder(calculateSortOrder());
                cardProcDs.addItem(cp);
                createProcLookup.setValue(null);

                cardProcTable.setSelected(cp);
                cardRolesFrame.initDefaultActors((Proc) value);
            }
        });
    }

    private int calculateSortOrder() {
        int i = 0;
        for (CardProc cardProc : getDsItems(cardProcDs)) {
            i = Math.max(i, cardProc.getSortOrder() == null ? 0 : cardProc.getSortOrder());
        }
        return ++i;
    }

    private void startProcess(final CardProc cp) {
        final Proc prevProc = card.getProc();
        DataService ds = getDsContext().getDataService();
        final Proc proc = ds.reload(cp.getProc(), "edit");
        final String prevCardProcState = cp.getState();
        card.setProc(proc);
        cp.setProc(proc);
        cp.setActive(true);
        cp.setState(null);
        final int prevStartCount = cp.getStartCount() == null ? 0 : cp.getStartCount();
        cp.setStartCount(prevStartCount + 1);

        if (!PersistenceHelper.isNew(card)) {
            LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(
                    new View(Card.class).addProperty("jbpmProcessId")
            );
            Card loadedCard = ds.load(lc);
            if (loadedCard.getJbpmProcessId() != null) {
                // already started in another transaction
                String msg = MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), "assignmentAlreadyFinished.message");
                App.getInstance().getWindowManager().showNotification(msg, NotificationType.ERROR);
                resetAfterUnsuccessfulStart(cp, prevProc, prevStartCount, prevCardProcState);
                return;
            }
        }

        final Window window = ComponentsHelper.getWindow(frame);
        if (window instanceof Window.Editor && ((Window.Editor) window).commit()) {
            // starting
            try {
                final FormManagerChain managerChain = FormManagerChain.getManagerChain(card, WfConstants.ACTION_START);
                managerChain.setCard(card);

                managerChain.setHandler(
                        new FormManagerChain.Handler() {
                            public void onSuccess(String comment) {
                                WfService wfs = ServiceLocator.lookup(WfService.NAME);
                                wfs.startProcess(card);
                                window.close(Window.COMMIT_ACTION_ID, true);
                                managerChain.doManagerAfter();
                            }

                            public void onFail() {
                                rollbackStartProcess(prevProc, prevStartCount, cp, prevCardProcState);
                                window.close("cancel", true);
                                
                                WindowInfo windowInfo = AppConfig.getInstance().getWindowConfig().getWindowInfo(window.getId());
                                App.getInstance().getWindowManager().openEditor(windowInfo, card, OpenType.THIS_TAB, Collections.<String, Object>singletonMap("tabName", "processTab"));
                            }
                        }
                );
                managerChain.doManagerBefore("");

            } catch (RuntimeException e) {
                rollbackStartProcess(prevProc, prevStartCount, cp, prevCardProcState);

                String msg = getMessage("runProcessFailed");
                App.getInstance().getAppLog().log(new LogItem(LogLevel.ERROR, msg, e));
                showNotification(msg, e.getMessage(), NotificationType.ERROR);

                window.close("cancel", true);
            }

        } else {
            resetAfterUnsuccessfulStart(cp, prevProc, prevStartCount, prevCardProcState);
        }
    }

    private void resetAfterUnsuccessfulStart(CardProc cp, Proc prevProc, int prevStartCount, String prevCardProcState) {
        card.setProc(prevProc);
        cp.setActive(false);
        cp.setStartCount(prevStartCount);
        cp.setState(prevCardProcState);
    }

    private void rollbackStartProcess(Proc prevProc, int prevStartCount, CardProc cp, String prevCardProcState) {
        DataService ds = getDsContext().getDataService();

        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(
                new View(Card.class).addProperty("proc")
        );
        Card loadedCard = ds.load(lc);
        loadedCard.setProc(prevProc);

        lc = new LoadContext(CardProc.class).setId(cp.getId()).setView(
                new View(CardProc.class).addProperty("active").addProperty("startCount").addProperty("state")
        );
        CardProc loadedCardProc = ds.load(lc);
        loadedCardProc.setActive(false);
        loadedCardProc.setStartCount(prevStartCount);
        loadedCardProc.setState(prevCardProcState);

        Set toCommit = new HashSet();
        toCommit.add(loadedCard);
        toCommit.add(loadedCardProc);
        CommitContext cc = new CommitContext(toCommit);
        ServiceLocator.getDataService().commit(cc);
    }

    protected void initRoles() {

        cardRolesFrame = getComponent("cardRolesFrame");
        cardRolesFrame.init();

    }

    public void setCard(final Card card) {
        Preconditions.checkArgument(card != null, "Card is null");
        this.card = card;

        Map<String, Object> params = Collections.<String, Object>singletonMap(
                "cardType",
                "%," + card.getMetaClass().getName() + ",%"
        );
        procDs.refresh(params);

        initCreateProcLookup();

        cardRolesFrame.setCard(card);
    }

    private void initCreateProcLookup() {
        List options = new ArrayList();
        for (Proc p : getDsItems(procDs)) {
            if (!alreadyAdded(p)) {
                options.add(p);
            }
        }
        options.add(0, createProcCaption);
        createProcLookup.setOptionsList(options);
        createProcLookup.setNullOption(createProcCaption);
    }

    private boolean alreadyAdded(Proc p) {
        for (CardProc cp : getDsItems(cardProcDs)) {
            if (cp.getProc().equals(p))
                return true;
        }
        return false;
    }


    protected <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<T>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
        this.enabled = enabled;
        cardRolesFrame.setEnabled(enabled);
    }

    private class StartProcessAction extends AbstractAction {

        public StartProcessAction() {
            super("runProc");
        }

        public void actionPerform(Component component) {
            Set selected = cardProcTable.getSelected();
            if (selected.size() != 1)
                return;

            final CardProc cp = (CardProc) selected.iterator().next();
            if (BooleanUtils.isTrue(cp.getActive()))
                return;

            Proc proc = cp.getProc();
            showOptionDialog(
                    getMessage("runProc.title"),
                    String.format(getMessage("runProc.msg"), proc.getName()),
                    MessageType.CONFIRMATION,
                    new Action[] {
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    startProcess(cp);
                                }
                            },
                            new DialogAction(DialogAction.Type.NO)
                    }
            );
        }
    }
    
    protected ProcRolePermissionsService getProcRolePermissionsService() {
        return ServiceLocator.lookup(ProcRolePermissionsService.NAME);
    }

    public CardRolesFrame getCardRolesFrame() {
        return cardRolesFrame;
    }
}