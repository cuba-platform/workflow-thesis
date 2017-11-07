/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.usergroup;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.UserGroup;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class UserGroupAdd extends AbstractWindow {

    @Inject
    protected TwinColumn twinColumn;

    @Inject
    protected CollectionDatasourceImpl userGroupsDs;

    @Inject
    protected Button searchButton;

    @Inject
    protected TextField searchText;

    protected Set selectedUsers = new HashSet<Entity>();
    protected Role secRole;
    protected Boolean hasInActiveUser = false;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initUserGroupDs(params);

        twinColumn.setStyleProvider(new TwinColumn.StyleProvider() {
            @Override
            public String getStyleName(Entity item, Object property, boolean selected) {
                return null;
            }

            @Override
            public String getItemIcon(Entity item, boolean selected) {
                if (item instanceof UserGroup)
                    return "theme:icons/wf-user-group-small.png";
                return null;
            }
        });

        java.util.List<User> users = (java.util.List<User>) params.get("Users");
        if (users != null)
            twinColumn.setValue(users);

        addAction(new AbstractAction("windowCommit") {
            @Override
            public void actionPerform(Component component) {
                Set values = twinColumn.getValue();
                processSelectedItems(values);
                close(COMMIT_ACTION_ID, true);
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {
            @Override
            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        initSearchButton();
    }

    protected void initSearchButton() {
        if (searchButton != null) {
            ClientConfig clientConfig = AppBeans.get(Configuration.class).getConfig(ClientConfig.class);
            searchButton.setAction(new AbstractAction("search", clientConfig.getFilterApplyShortcut()) {
                @Override
                public void actionPerform(Component component) {
                    HashSet selectedItems = twinColumn.getValue();
                    String requiredText = searchText.getValue();
                    Map<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("selectedItems", selectedItems);
                    parameters.put("requiredText", requiredText);
                    parameters.put("secRole", secRole);
                    userGroupsDs.refresh(parameters);
                    twinColumn.setValue(selectedItems);
                }

                @Override
                public String getCaption() {
                    return getMessage("actions.Apply");
                }
            });

            addAction(searchButton.getAction());
        }
    }

    protected void initUserGroupDs(Map<String, Object> params) {
        secRole = (Role) params.get("secRole");
        if (secRole != null) {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("secRole", secRole);
            userGroupsDs.refresh(parameters);
        }
    }

    protected void processSelectedItems(Set<Entity> selectedItems) {
        hasInActiveUser = false;
        Set<UserGroup> userGroups = new HashSet<>();
        for (Entity item : selectedItems) {
            if (item instanceof UserGroup)
                userGroups.add((UserGroup) item);
        }

        selectedItems.removeAll(userGroups);

        for (UserGroup userGroup : userGroups) {
            for (User user : userGroup.getUsers())
                if (user.getActive())
                    selectedUsers.add(user);
                else
                    hasInActiveUser = true;
        }
        selectedUsers.addAll(selectedItems);
    }

    public Set<User> getSelectedUsers() {
        return selectedUsers;
    }
}
