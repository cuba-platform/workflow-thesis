/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 28.06.2010 13:50:48
 *
 * $Id$
 */

package com.haulmont.workflow.web.ui.base;


import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TextField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.settings.Settings;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.app.MailService;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.cuba.core.entity.Entity;

import java.util.*;
import java.util.List;

public class CardSend extends AbstractWindow {

    protected CollectionDatasourceImpl<User, UUID> tmpUserDs;
    protected CollectionDatasource<User, UUID> userDs;
    protected String createUserCaption;
    protected String createAllUsersCaption;
    protected String createAnyUserCaption;
    protected String createCreatorUserCaption;
    protected LookupField createUserLookup;
    protected Table usersTable;
    protected Card card;
    protected CardComment parent;
    protected CheckBox notifyByCardInfo;
    protected List<CardRole> roles;

    public CardSend(IFrame frame) {
        super(frame);
    }

    protected void init(Map<String, Object> params) {
        super.init(params);
        setHeight("400px");
        card = (Card) params.get("item");
        if(PersistenceHelper.isNew(card))
            throw new RuntimeException("Card is new");
        if (card == null)
            throw new RuntimeException("Card null");
        roles = getCardRoles(card);
        notifyByCardInfo = getComponent("notifyByCardInfo");
        notifyByCardInfo.setValue(true);
        parent = (CardComment)params.get("parent");
        tmpUserDs = getDsContext().get("tmpUserDs");
        tmpUserDs.valid();
        if (parent != null) {
            tmpUserDs.addItem(parent.getSender());    
        }
        createUserCaption = getMessage("cardSend.createUserCaption");
        createAllUsersCaption = getMessage("cardSend.createAllUsersCaption");
        createAnyUserCaption = getMessage("cardSend.createAnyUserCaption");
        createCreatorUserCaption = getMessage("cardSend.createCreatorUserCaption");
        createUserLookup = getComponent("createUserLookup");
        userDs = getDsContext().get("userDs");
        usersTable = getComponent("usersTable");
        userDs.refresh();
        initCreateUserLookup();

        tmpUserDs.addListener(new CollectionDsListenerAdapter<User>() {
            @Override
            public void collectionChanged(CollectionDatasource ds, CollectionDatasourceListener.Operation operation) {
                initCreateUserLookup();
            }
        });

        createUserLookup.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, final Object value) {
                if ((value == null) || createUserCaption.equals(value))
                    return;
                if (createAnyUserCaption.equals(value)) {
                    Map<String, Object> lookupParams = Collections.<String, Object>singletonMap("multiSelect", "true");
                    openLookup("sec$User.lookup", new Lookup.Handler() {
                        public void handleLookup(Collection items) {
                            for (Object item : items) {
                                User user = (User) item;
                                if (tmpUserDs.containsItem(user.getUuid())) continue;
                                tmpUserDs.addItem(user);
                            }
                        }

                    }, WindowManager.OpenType.DIALOG, lookupParams);

                } else if (createAllUsersCaption.equals(value)) {
                    if (roles != null) {
                        User user;
                        for (CardRole cardRole : roles) {
                            user = cardRole.getUser();
                            if (user != null && !alreadyAdded(user)) {
                                tmpUserDs.addItem(user);
                            }
                        }
                    }
                    User user = card.getCreator();
                    if (user != null && !alreadyAdded(user)) {
                        tmpUserDs.addItem(user);
                    }
                } else if (createCreatorUserCaption.equals(value)) {
                    User user = card.getCreator();
                    if (user != null && !alreadyAdded(user)) {
                        tmpUserDs.addItem(user);
                    }
                } else {;
                    if (roles != null) {
                        User user;
                        for (CardRole cardRole : roles) {
                            user = cardRole.getUser();
                            if (user != null && !alreadyAdded(user)) {
                                ProcRole procRole = cardRole.getProcRole();
                                if (procRole != null && procRole.getName().equals(value)) {
                                    tmpUserDs.addItem(user);
                                }
                            }
                        }
                    }
                }
                createUserLookup.setValue(null);
            }
        });

        ((Button) getComponent("removeUser")).setAction(new AbstractAction("usersTable.remove") {
            public void actionPerform(Component component) {
                if (usersTable.getSingleSelected() != null)
                    tmpUserDs.removeItem((User) usersTable.getSingleSelected());
            }

            @Override
            public String getCaption() {
                return getMessage("cardSend.remove");
            }
        });
        addAction(new AbstractAction("windowCommit") {
            public void actionPerform(Component component) {
                TextField comment = getComponent("commentText");
                String commentStr = comment.getValue();
                if (commentStr != null && commentStr.length() > 0) {
                    List<User> users = new LinkedList<User>();
                    if (tmpUserDs.size() > 0) {
                        MailService mailService = ServiceLocator.lookup(MailService.JNDI_NAME);
                        for (UUID uuid : tmpUserDs.getItemIds()) {
                            users.add(tmpUserDs.getItem(uuid));
                        }
                        mailService.sendCardMail(card, commentStr, users, getNotificationScript());
                    } else {
                        showNotification(getMessage("cardSend.noUsers"), IFrame.NotificationType.WARNING);
                        return;
                    }
                    if ((Boolean) notifyByCardInfo.getValue()) {
                        Set<Entity> toCommit = new HashSet<Entity>();
                        for (UUID uuid : tmpUserDs.getItemIds()) {
                            toCommit.add(createCardInfo(card, tmpUserDs.getItem(uuid), commentStr));
                        }
                        CommitContext commitContext = new CommitContext(toCommit);
                        ServiceLocator.getDataService().commit(commitContext);
                    }
                    Set<Entity> toCommit = new HashSet<Entity>();
                    CardComment cardComment = new CardComment();
                    if(parent != null){
                        cardComment.setAddressees(users);
                        cardComment.setSender(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser());
                        cardComment.setCard(card);
                        cardComment.setComment(commentStr);
                        cardComment.setParent(parent);
                    }else{
                        cardComment.setAddressees(users);
                        cardComment.setSender(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser());
                        cardComment.setCard(card);
                        cardComment.setComment(commentStr);
                    }
                    toCommit.add(cardComment);
                    CommitContext commitContext = new CommitContext(toCommit);
                    ServiceLocator.getDataService().commit(commitContext);
                    close(Window.COMMIT_ACTION_ID, true);
                } else {
                    showNotification(getMessage("cardSend.noComment"), IFrame.NotificationType.WARNING);
                }
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {
            public void actionPerform(Component component) {
                close("cancel", true);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Cancel");
            }
        });
    }

    protected String getNotificationScript() {
        return "process/EmailNotification.groovy";
    }

    protected <T extends Entity<UUID>> List<T> getDsItems(CollectionDatasource<T, UUID> ds) {
        List<T> items = new ArrayList<T>();
        for (UUID id : ds.getItemIds()) {
            items.add(ds.getItem(id));
        }
        return items;
    }

    protected boolean alreadyAdded(User user) {
        for (User _user : getDsItems(tmpUserDs)) {
            if (_user.equals(user))
                return true;
        }
        return false;
    }

    protected List<CardRole> getCardRoles(Card card){
        LoadContext ctx = new LoadContext(CardRole.class);
        ctx.setView("card-edit");
        ctx.setQueryString("select cr from wf$CardRole cr where cr.card.id = :cardId and " +
                "cr.procRole.id in (select pr.id from wf$ProcRole pr where pr.proc.id = :procId)")
                .addParameter("cardId", card).addParameter("procId",card.getProc());
        return ServiceLocator.getDataService().loadList(ctx);
    }

    protected void initCreateUserLookup() {
        List<String> options = new ArrayList<String>();
        if (roles != null) {
            User user;
            for (CardRole cardRole : roles) {
                user = cardRole.getUser();
                if (user != null) {
                    ProcRole procRole = cardRole.getProcRole();
                    if (procRole != null && !alreadyAdded(user)) {
                        options.add(procRole.getName());
                    }
                }
            }
            user = card.getCreator();
            if (user != null) {
                if (!alreadyAdded(user)) {
                    options.add(createCreatorUserCaption);
                }
            }
            if (options.size() > 0) {
                options.add(createAllUsersCaption);
            }
        }
        options.add(createAnyUserCaption);
        options.add(0, createUserCaption);
        createUserLookup.setOptionsList(options);
        createUserLookup.setNullOption(createUserCaption);
    }

    @Override
    public void applySettings(Settings settings) {
        super.applySettings(settings);
        com.vaadin.ui.Window window = WebComponentsHelper.unwrap(frame).getWindow();
        if (window.isModal()) {
            window.setClosable(false);
            window.setResizable(false);
        }
    }

    protected CardInfo createCardInfo(Card card, User user, String comment) {
        CardInfo ci = new CardInfo();
        ci.setCard(card);
        ci.setType(5);
        ci.setUser(user);
        Proc proc  = card.getProc();
        if(proc != null)
            ci.setJbpmExecutionId(proc.getJbpmProcessKey());
        ci.setActivity("Comment");
        ci.setDescription(card.getDescription()+"("+comment+")");
        return ci;
    }
}