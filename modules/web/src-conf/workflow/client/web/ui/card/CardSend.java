/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 28.06.2010 13:50:48
 *
 * $Id$
 */

package workflow.client.web.ui.card;


import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.gui.data.*;
import com.haulmont.workflow.core.app.MailService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.ProcRole;
import com.haulmont.workflow.web.ui.base.AbstractCardEditor;
import com.haulmont.cuba.core.entity.Entity;

import java.util.*;
import java.util.List;

public class CardSend extends AbstractCardEditor {

    protected CollectionDatasourceImpl<User, UUID> tmpUserDs;
    protected CollectionDatasource<User, UUID> userDs;
    protected String createUserCaption;
    protected String createAllUsersCaption;
    protected String createAnyUserCaption;
    protected String createCreatorUserCaption;
    protected LookupField createUserLookup;
    protected Table usersTable;
    protected Card card;

    public CardSend(IFrame frame) {
        super(frame);
    }

    protected void init(Map<String, Object> params) {
        super.init(params);
        card = (Card) params.get("item");
        if (card == null)
            throw new RuntimeException("Card null");
        tmpUserDs = getDsContext().get("tmpUserDs");
        tmpUserDs.valid();
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
                    List<CardRole> roles = card.getRoles();
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
                } else {
                    List<CardRole> roles = card.getRoles();
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
                    if (tmpUserDs.size() > 0) {
                        MailService mailService = ServiceLocator.lookup(MailService.JNDI_NAME);
                        List<User> users = new LinkedList<User>();
                        for (UUID uuid : tmpUserDs.getItemIds()) {
                            users.add(tmpUserDs.getItem(uuid));
                        }
                        mailService.sendCardMail(card, commentStr, users);
                    } else {
                        showNotification(getMessage("cardSend.noUsers"), IFrame.NotificationType.WARNING);
                        return;
                    }

                    close("cancel", true);
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

    protected void initCreateUserLookup() {
        List<String> options = new ArrayList<String>();
        List<CardRole> roles = card.getRoles();
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

    public void setItem(Entity item) {
        super.setItem(item);
    }

    @Override
    protected boolean isCommentVisible() {
        return true;
    }
}