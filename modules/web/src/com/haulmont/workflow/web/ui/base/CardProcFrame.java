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
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.app.LinkColumnHelper;
import com.haulmont.cuba.web.log.LogItem;
import com.haulmont.cuba.web.log.LogLevel;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.FormManagerChain;
import org.apache.commons.lang.BooleanUtils;

import java.util.*;
import java.util.List;

import static com.haulmont.cuba.gui.WindowManager.OpenType;

public class CardProcFrame extends AbstractFrame {

    public interface Listener {
        void afterInitDefaultActors(Proc proc, CollectionDatasource currentCardRolesDs);
    }

    private Card card;
    private boolean enabled = true;

    private CollectionDatasource<CardRole, UUID> cardRolesDs;
    private CollectionDatasource<ProcRole, UUID> procRolesDs;
    private CollectionDatasource<Proc, UUID> procDs;
    private CollectionDatasource<CardProc, UUID> cardProcDs;
    private CardProcRolesDatasource tmpCardRolesDs;
    private LookupField createProcLookup;
    private LookupField createRoleLookup;
    private Table cardProcTable;
    private Table rolesTable;
    protected List<Component> rolesActions = new ArrayList<Component>();
    protected AbstractAction startProcessAction;

    private String createProcCaption;
    private String createRoleCaption;

    private Set<Listener> listeners = new HashSet<Listener>();

    public CardProcFrame(IFrame frame) {
        super(frame);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public void init() {
        cardProcDs = getDsContext().get("cardProcDs");
        Preconditions.checkState(cardProcDs != null, "Enclosing window must declare 'cardProcsDs' datasource");

        cardRolesDs = getDsContext().get("cardRolesDs");
        Preconditions.checkState(cardRolesDs != null, "Enclosing window must declare 'cardRolesDs' datasource");

        initProc();
        initRoles();
    }

    private void initProc() {
        createProcCaption = getMessage("createProcCaption");
        createProcLookup = getComponent("createProcLookup");

        procDs = getDsContext().get("procDs");

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

        cardProcDs.addListener(
                new DsListenerAdapter<CardProc>() {
                    @Override
                    public void itemChanged(Datasource<CardProc> ds, CardProc prevItem, CardProc item) {
                        tmpCardRolesDs.fillForProc(item);
                        procChanged(item == null ? null : item.getProc());

                        boolean enabled = item != null && !BooleanUtils.isTrue(item.getActive());
                        removeAction.setEnabled(enabled);

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
                initDefaultActors((Proc) value);
            }
        });

        cardProcDs.addListener(new CollectionDsListenerAdapter<CardProc>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                initCreateProcLookup();
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
        card.setProc(proc);
        cp.setProc(proc);
        cp.setActive(true);
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
                resetAfterUnsuccessfulStart(cp, prevProc, prevStartCount);
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
                                rollbackStartProcess(prevProc, prevStartCount, cp);
                                window.close("cancel", true);
                            }
                        }
                );
                managerChain.doManagerBefore("");

            } catch (RuntimeException e) {
                rollbackStartProcess(prevProc, prevStartCount, cp);

                String msg = getMessage("runProcessFailed");
                App.getInstance().getAppLog().log(new LogItem(LogLevel.ERROR, msg, e));
                showNotification(msg, e.getMessage(), NotificationType.ERROR);

                window.close("cancel", true);
            }

        } else {
            resetAfterUnsuccessfulStart(cp, prevProc, prevStartCount);
        }
    }

    private void resetAfterUnsuccessfulStart(CardProc cp, Proc prevProc, int prevStartCount) {
        card.setProc(prevProc);
        cp.setActive(false);
        cp.setStartCount(prevStartCount);
    }

    private void rollbackStartProcess(Proc prevProc, int prevStartCount, CardProc cp) {
        DataService ds = getDsContext().getDataService();

        LoadContext lc = new LoadContext(Card.class).setId(card.getId()).setView(
                new View(Card.class).addProperty("proc")
        );
        Card loadedCard = ds.load(lc);
        loadedCard.setProc(prevProc);

        lc = new LoadContext(CardProc.class).setId(cp.getId()).setView(
                new View(CardProc.class).addProperty("active").addProperty("startCount")
        );
        CardProc loadedCardProc = ds.load(lc);
        loadedCardProc.setActive(false);
        loadedCardProc.setStartCount(prevStartCount);

        Set toCommit = new HashSet();
        toCommit.add(loadedCard);
        toCommit.add(loadedCardProc);
        CommitContext cc = new CommitContext(toCommit);
        ServiceLocator.getDataService().commit(cc);
    }

    private void initRoles() {
        tmpCardRolesDs = getDsContext().get("tmpCardRolesDs");
        tmpCardRolesDs.valid();

        tmpCardRolesDs.addListener(new CollectionDsListenerAdapter<CardRole>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, Operation operation) {
                initCreateRoleLookup();
            }
        });

        procRolesDs = getDsContext().get("procRolesDs");
        createRoleCaption = getMessage("createRoleCaption");
        createRoleLookup = getComponent("createRoleLookup");

        rolesActions.add(createRoleLookup);
        rolesActions.add(getComponent("editRole"));
        rolesActions.add(getComponent("removeRole"));

        rolesTable = getComponent("rolesTable");
        TableActionsHelper rolesTH = new TableActionsHelper(this, rolesTable);

        final CollectionDatasource rolesTableDs = rolesTable.getDatasource();
        rolesTable.addAction(new AbstractAction("edit") {
            public void actionPerform(Component component) {
                Entity entity = rolesTableDs.getItem();
                if (entity == null) return;
                Object users = getUsersByProcRole(((CardRole) entity).getProcRole());
                openEditor("wf$CardRole.edit", entity, OpenType.DIALOG,
                        Collections.singletonMap("users", users), rolesTableDs);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), "actions.Edit");
            }
        });

        rolesTableDs.addListener(new DsListenerAdapter() {
            @Override
            public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                super.stateChanged(ds, prevState, state);
                if (state.equals(Datasource.State.VALID) && isEnabled()) {
                    LinkColumnHelper.initColumn(rolesTable, "procRole.name", new LinkColumnHelper.Handler() {
                        public void onClick(final Entity entity) {
                            Object users = getUsersByProcRole(((CardRole) entity).getProcRole());
                            openEditor("wf$CardRole.edit", entity, OpenType.DIALOG,
                                    Collections.singletonMap("users", users), rolesTableDs);
                        }
                    });
                }
            }
        });

        rolesTH.createRemoveAction(false);

        createRoleLookup.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                if ((value == null) || createRoleCaption.equals(value))
                    return;

                CardRole cr = new CardRole();
                ProcRole procRole = (ProcRole) value;
                Role secRole = procRole.getRole();

                Map<String, Object> params = new HashMap<String, Object>();
                params.put("procRole", procRole);
                params.put("secRole", secRole);
                params.put("proc", cardProcDs.getItem().getProc());
                params.put("users", getUsersByProcRole(procRole));
                final Window.Editor cardRoleEditor = openEditor("wf$CardRole.edit", cr, OpenType.DIALOG, params);
                cardRoleEditor.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                            CardRole cardRole = (CardRole)cardRoleEditor.getItem();
                            cardRole.setCode(cardRole.getProcRole().getCode());
                            tmpCardRolesDs.addItem(cardRole);
                            cardRole.setCard(card);
                        }
                    }
                });

                createRoleLookup.setValue(null);
            }
        });
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

//        for (Component component : rolesActions) {
//            component.setEnabled(card.getProc() != null);
//        }
    }

    public void procChanged(Proc proc) {
        procRolesDs.refresh();
        initCreateRoleLookup();

        for (Component component : rolesActions) {
            component.setEnabled(proc != null && isEnabled());
        }
    }

    public void initDefaultActors(Proc proc) {
        if (!tmpCardRolesDs.getItemIds().isEmpty())
            return;

        LoadContext ctx = new LoadContext(DefaultProcActor.class);
        ctx.setQueryString("select a from wf$DefaultProcActor a where a.procRole.proc.id = :procId")
            .addParameter("procId", proc.getId());
        ctx.setView("edit");
        List<DefaultProcActor> dpaList = ServiceLocator.getDataService().loadList(ctx);
        for (DefaultProcActor dpa : dpaList) {
            CardRole cr = new CardRole();
            cr.setProcRole(dpa.getProcRole());
            cr.setCode(dpa.getProcRole().getCode());
            cr.setUser(dpa.getUser());
            cr.setCard(card);
            cr.setNotifyByEmail(dpa.getNotifyByEmail());
            tmpCardRolesDs.addItem(cr);
        }

        // if there is a role with AssignToCreator property set up, and this role is not assigned
        // by DefaultProcActor list, assign this role to the current user
        for (UUID procRoleId : procRolesDs.getItemIds()) {
            ProcRole procRole = procRolesDs.getItem(procRoleId);
            if (BooleanUtils.isTrue(procRole.getAssignToCreator())) {
                boolean found = false;
                for (UUID cardRoleId : tmpCardRolesDs.getItemIds()) {
                    CardRole cardRole = tmpCardRolesDs.getItem(cardRoleId);
                    if (procRole.equals(cardRole.getProcRole())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    CardRole cr = new CardRole();
                    cr.setProcRole(procRole);
                    cr.setCode(procRole.getCode());
                    cr.setUser(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser());
                    cr.setCard(card);
                    cr.setNotifyByEmail(true);
                    tmpCardRolesDs.addItem(cr);
                }
            }
        }

        for (Listener listener : listeners) {
            listener.afterInitDefaultActors(proc, tmpCardRolesDs);
        }
    }

    public void setProcActor(Proc proc, String roleCode, User user, boolean notifyByEmail) {
        CardRole cardRole = null;

        for (CardRole cr : getDsItems(tmpCardRolesDs)) {
            if (roleCode.equals(cr.getCode())) {
                cardRole = cr;
                break;
            }
        }

        //If card role with given code doesn't exist we'll create a new one
        if (cardRole == null) {
            cardRole = new CardRole();

            if (proc.getRoles() == null) {
                proc = getDsContext().getDataService().reload(proc, "edit");
            }

            ProcRole procRole = null;
            for (ProcRole pr : proc.getRoles()) {
                if (roleCode.equals(pr.getCode())) {
                    procRole = pr;
                }
            }
            if (procRole == null)
                return;

            cardRole.setProcRole(procRole);
            cardRole.setCode(roleCode);
            cardRole.setCard(card);
            cardRole.setNotifyByEmail(notifyByEmail);
            tmpCardRolesDs.addItem(cardRole);
        }
        cardRole.setUser(user);
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

    private void initCreateRoleLookup() {
        // add ProcRole if it has multiUser == true or hasn't been added yet
        List options = new ArrayList();
        for (ProcRole pr : getDsItems(procRolesDs)) {
            if (BooleanUtils.isTrue(pr.getMultiUser()) || !alreadyAdded(pr)) {
                options.add(pr);
            }
        }
        options.add(0, createRoleCaption);
        createRoleLookup.setOptionsList(options);
        createRoleLookup.setNullOption(createRoleCaption);
    }

    private boolean alreadyAdded(Proc p) {
        for (CardProc cp : getDsItems(cardProcDs)) {
            if (cp.getProc().equals(p))
                return true;
        }
        return false;
    }

    private boolean alreadyAdded(ProcRole pr) {
        for (CardRole cr : getDsItems(tmpCardRolesDs)) {
            if (cr.getProcRole().equals(pr))
                return true;
        }
        return false;
    }

    private <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<T>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

    private Set<UUID> getUsersByProcRole(ProcRole procRole) {
        if (procRole == null) {
            return null;
        }
        Set<UUID> res = new HashSet<UUID>();
        Collection<UUID> crIds = cardRolesDs.getItemIds();
        for (UUID crId : crIds) {
            CardRole cr = cardRolesDs.getItem(crId);
            if (procRole.equals(cr.getProcRole()) && cr.getUser() != null) {
                res.add(cr.getUser().getId());
            }
        }
        return res;
    }

//    public void disable() {
//        if (rolesTable.getActions() != null) {
//            for (Action action : rolesTable.getActions()) {
//                action.setEnabled(false);
//            }
//        }
//        createRoleLookup.setEditable(false);
//        LinkColumnHelper.removeColumn(rolesTable, "procRole.name");
//
//    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
//        super.setEnabled(enabled);
        this.enabled = enabled;

        if (rolesTable.getActions() != null) {
            for (Action action : rolesTable.getActions()) {
                action.setEnabled(enabled);
            }
        }
        for (Component action : rolesActions) {
            action.setEnabled(enabled);
        }
        if (!enabled) {
            LinkColumnHelper.removeColumn(rolesTable, "procRole.name");
        }
    }

    public static class CardProcRolesDatasource extends CollectionDatasourceImpl<CardRole, UUID> {

        private CollectionDatasource<CardRole, UUID> cardRolesDs = getDsContext().get("cardRolesDs");
        private boolean fill;

        public CardProcRolesDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
            super(context, dataservice, id, metaClass, viewName);
            cardRolesDs = getDsContext().get("cardRolesDs");
        }

        @Override
        public void addItem(CardRole item) throws UnsupportedOperationException {
            super.addItem(item);
            if (!fill)
                cardRolesDs.addItem(item);
        }

        @Override
        public void removeItem(CardRole item) throws UnsupportedOperationException {
            super.removeItem(item);
            if (!fill)
                cardRolesDs.removeItem(item);
        }

        public void fillForProc(CardProc cardProc) {
            fill = true;
            try {
                for (UUID id : new ArrayList<UUID>(getItemIds())) {
                    removeItem(getItem(id));
                }
                if (cardProc != null) {
                    for (UUID id : cardRolesDs.getItemIds()) {
                        CardRole cardRole = cardRolesDs.getItem(id);
                        if (cardRole.getProcRole().getProc().equals(cardProc.getProc())) {
                            addItem(cardRole);
                        }
                    }
                }
                setModified(false);
            } finally {
                fill = false;
            }
        }
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
}