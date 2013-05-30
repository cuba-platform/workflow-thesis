/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
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
import com.vaadin.data.Property;
import org.apache.commons.lang.BooleanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class CardProcFrame extends AbstractFrame {

    protected Card card;
    private boolean enabled = true;

    private CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected CollectionDatasource<Proc, UUID> procDs;
    protected CollectionDatasource<CardProc, UUID> cardProcDs;
    protected CollectionDatasource<CardProc, UUID> subProcessCardProcDs;
    private List<String> excludedProcessesCodes = new ArrayList<String>();
    protected LookupField createProcLookup;
    protected LookupField subProcessLookup;
    protected Label subProcessLookupLabel;
    protected Table cardProcTable;
    protected AbstractAction startProcessAction;
    protected CardRolesFrame cardRolesFrame;
    private Button removeProc;

    private String createProcCaption;

    protected ProcRolePermissionsService procRolePermissionsService;

    public void init() {
        cardProcDs = getDsContext().get("cardProcDs");
        Preconditions.checkState(cardProcDs != null, "Enclosing window must declare 'cardProcsDs' datasource");
        removeProc = getComponent("removeProc");

        initProc();
        initRoles();

        procRolePermissionsService = getProcRolePermissionsService();
    }

    private void initProc() {
        createProcCaption = getMessage("createProcCaption");
        createProcLookup = getComponent("createProcLookup");
        subProcessLookup = getComponent("subProcessLookup");
        subProcessLookupLabel = getComponent("subProcessLookupLabel");
        subProcessLookup.setVisible(false);
        subProcessLookupLabel.setVisible(false);


        cardRolesDs = getDsContext().get("cardRolesDs");
        subProcessCardProcDs = getDsContext().get("subProcessCardProcDs");
        procDs = getDsContext().get("procDs");

        cardProcTable = getComponent("cardProcTable");
        final com.vaadin.ui.Table vCardProcTable = ( com.vaadin.ui.Table)WebComponentsHelper.unwrap(cardProcTable);
        vCardProcTable.addListener(new com.vaadin.ui.Table.ValueChangeListener(){
             public void valueChange(Property.ValueChangeEvent event){
                 Object uuid = vCardProcTable.getValue();
                 if(uuid != null){
                     CardProc proc = (CardProc)cardProcTable.getDatasource().getItem(uuid);
                     if(proc != null){
                         if(proc.getStartCount() < 1){
                             removeProc.setEnabled(true);
                             return;
                         }
                     }
                 }
                 removeProc.setEnabled(false);
             }
        });
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
            createStartProcessAction();
            initStartProcessAction();
        }

        final boolean removeActionEnabled = accessData == null || accessData.getRemoveCardProcessEnabled();

        cardProcDs.addListener(
                new CollectionDsListenerAdapter<CardProc>() {
                    @Override
                    public void itemChanged(Datasource<CardProc> ds, CardProc prevItem, CardProc item) {
                        cardRolesFrame.setCardProc(item);
                        if (item != null) {
                            subProcessCardProcDs.refresh(Collections.<String, Object>singletonMap("cardProc", item.getId()));
                            subProcessLookup.setVisible(subProcessCardProcDs.size() > 1);
                            subProcessLookupLabel.setVisible(subProcessLookup.isVisible());
                            subProcessLookup.setNullOption(item);
                            subProcessLookup.setValue(item);
                        }

                        cardRolesFrame.procChanged(item == null ? null : item.getProc());
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
                            Boolean notExcluded = item == null ? true : !excludedProcessesCodes.contains(item.getProc().getCode());
                            startProcessAction.setEnabled(enabled && notExcluded);
                        }
                    }

                    @Override
                    public void collectionChanged(CollectionDatasource ds,
                                                  CollectionDatasourceListener.Operation operation,
                                                  List<CardProc> items) {
                        initCreateProcLookup();
                    }

                }
        );

        subProcessLookup.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                if (value == null) {
                    CardProc selectedRole = cardProcTable.getSingleSelected();
                    if (selectedRole != null)
                        cardRolesFrame.procChanged(selectedRole.getProc());
                } else {
                    CardProc selectedProc = (CardProc) value;
                    cardRolesFrame.procChanged(selectedProc.getProc(), selectedProc.getCard());
                }
            }
        });

        boolean enabled = accessData == null || accessData.getAddCardProcessEnabled();
        createProcLookup.setEditable(enabled);

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

    protected void createStartProcessAction() {
        startProcessAction = new StartProcessAction();
    }

    protected void initStartProcessAction() {
        startProcessAction.setEnabled(false);
        cardProcTable.addAction(startProcessAction);
        Button startProcBtn = getComponent("startProc");
        startProcBtn.setAction(startProcessAction);
        startProcBtn.setVisible(true);
    }

    public int calculateSortOrder() {
        int i = 0;
        for (CardProc cardProc : getDsItems(cardProcDs)) {
            i = Math.max(i, cardProc.getSortOrder() == null ? 0 : cardProc.getSortOrder());
        }
        return ++i;
    }

    public void startProcess(final CardProc cp) {
        refreshCard();
        final Proc prevProc = card.getProc();
        DataSupplier ds = getDsContext().getDataSupplier();
        final String prevCardProcState = cp.getState();

        final int prevStartCount = cp.getStartCount() == null ? 0 : cp.getStartCount();

        if (!PersistenceHelper.isNew(card)) {
            LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(
                    new View(Card.class).addProperty("jbpmProcessId")
            );
            Card loadedCard = ds.load(lc);
            if (loadedCard.getJbpmProcessId() != null) {
                // already started in another transaction
                String msg = AppBeans.get(Messages.class).getMainMessage("assignmentAlreadyFinished.message");
                App.getInstance().getWindowManager().showNotification(msg, NotificationType.ERROR);
                resetAfterUnsuccessfulStart(cp, prevProc, prevStartCount, prevCardProcState);
                return;
            }
        }

        final Window window = ComponentsHelper.getWindow(frame);
        if (window instanceof Window.Editor && ((Window.Editor) ((WebWindow.Editor) window).getWrapper()).commit()) {
            refreshCard();

            final Proc proc = ds.reload(cp.getProc(), "start-process");
            card.setProc(proc);
            cp.setProc(proc);
            cp.setState(null);

            // starting
            try {
                final FormManagerChain managerChain = FormManagerChain.getManagerChain(card, WfConstants.ACTION_START);
                managerChain.setCard(card);

                managerChain.setHandler(
                        new FormManagerChain.Handler() {
                            @Override
                            public void onSuccess(String comment) {
                                List<Entity> commitInstances = new ArrayList<>();
                                commitInstances.add(card);
                                AppBeans.get(DataService.class).commit(new CommitContext(commitInstances));

                                WfService wfs = AppBeans.get(WfService.NAME);
                                wfs.startProcess(card);
                                window.close(Window.COMMIT_ACTION_ID, true);
                                managerChain.doManagerAfter();
                            }

                            @Override
                            public void onFail() {
                                rollbackStartProcess(prevProc, prevStartCount, cp, prevCardProcState);

                                Object editor = null;
                                if (window instanceof WrappedWindow) {
                                    editor = ((WrappedWindow) window).getWrapper();
                                }

                                if (editor instanceof AbstractCardEditor) {
                                    ((AbstractCardEditor) editor).reopen(Collections.<String, Object>singletonMap("tabName", "processTab"));
                                } else
                                    window.close("cancel", true);
                            }
                        }
                );
                managerChain.doManagerBefore("");

            } catch (RuntimeException e) {
                rollbackStartProcess(prevProc, prevStartCount, cp, prevCardProcState);

                String msg = getMessage("runProcessFailed");
                App.getInstance().getAppLog().log(new LogItem(LogLevel.ERROR, msg, e));
                String message;
                if (e.getCause() instanceof InvocationTargetException) {
                    message = ((InvocationTargetException) e.getCause()).getTargetException().getMessage();
                } else message = e.getMessage();
                showNotification(msg, message, NotificationType.ERROR);

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
        DataSupplier ds = getDsContext().getDataSupplier();

        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(
                new View(Card.class).addProperty("proc").addProperty("jbpmProcessId")
        );
        Card loadedCard = ds.load(lc);

        //If process start failed because of another concurrently started process, jbpmProcessId will be not empty.
        //In this case we will not restore previous value of card.proc field
        if (loadedCard.getJbpmProcessId() == null) {
            loadedCard.setProc(prevProc);
        }

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

        Map<String, Object> params = new HashMap<>();
        params.put("cardType", "%," + card.getMetaClass().getName() + ",%");
        params.put("userId", UserSessionProvider.currentOrSubstitutedUserId());
        procDs.refresh(params);

        initCreateProcLookup();

        cardRolesFrame.setCard(card);
    }

    protected void initCreateProcLookup() {
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

    protected boolean alreadyAdded(Proc p) {
        for (CardProc cp : getDsItems(cardProcDs)) {
            if (cp.getProc() != null  && cp.getProc().equals(p))
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

    protected class StartProcessAction extends AbstractAction {

        public StartProcessAction() {
            super("runProc");
        }

        public void actionPerform(Component component) {
            Set<CardProc> selected = cardProcTable.getSelected();
            if (selected.size() != 1)
                return;

            final CardProc cp = selected.iterator().next();
            if (BooleanUtils.isTrue(cp.getActive()))
                return;

            Proc proc = cp.getProc();
            showOptionDialog(
                    getMessage("runProc.title"),
                    String.format(getMessage("runProc.msg"), proc.getName()),
                    MessageType.CONFIRMATION,
                    new Action[]{
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

    private void refreshCard() {
        card = (Card) getDsContext().get("cardDs").getItem();
    }

    public void setExcludedProcesses(List<String> excludedProcessesCodes){
        this.excludedProcessesCodes = excludedProcessesCodes;
    }
}
