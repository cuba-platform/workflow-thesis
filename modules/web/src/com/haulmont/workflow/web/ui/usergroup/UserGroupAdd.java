package com.haulmont.workflow.web.ui.usergroup;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.UserGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Eugeniy Murzin
 * @version $Id$
 */
public class UserGroupAdd extends AbstractWindow {
    protected TwinColumn twinColumn;
    protected CollectionDatasourceImpl userGroupsDs;
    protected Set selectedUsers = new HashSet<Entity>();
    protected Role secRole;
    protected Boolean hasInActiveUser = false;
    protected Button searchButton;
    protected TextField searchText;

    public UserGroupAdd(IFrame frame) {
        super(frame);
    }

    public void init(Map<String, Object> params) {
        super.init(params);
        twinColumn = getComponent("twinColumn");
        userGroupsDs = getDsContext().get("userGroupsDs");
        searchButton = getComponent("searchButton");
        searchText = getComponent("searchText");

        initUserGroupDs(params);

        twinColumn.setStyleProvider(new TwinColumn.StyleProvider() {
            @Override
            public String getStyleName(Entity item, Object property, boolean selected) {
                return null;
            }

            @Override
            public String getItemIcon(Entity item, boolean selected) {
                if (item instanceof UserGroup)
                    return "theme:icons/user-group-small.png";
                return null;
            }
        });

        java.util.List<User> users = (java.util.List<User>) params.get("Users");
        if (users != null)
            twinColumn.setValue(users);

        addAction(new AbstractAction("windowCommit") {
            public void actionPerform(Component component) {
                Set values = twinColumn.getValue();
                processSelectedItems(values);
                close(COMMIT_ACTION_ID, true);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {
            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        final Action action = new AbstractAction("search") {
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
        };
        com.vaadin.ui.Button btn = (com.vaadin.ui.Button) WebComponentsHelper.unwrap(searchButton);
        btn.setCaption(action.getCaption());
        btn.addListener(new com.vaadin.ui.Button.ClickListener() {
            @Override
            public void buttonClick(com.vaadin.ui.Button.ClickEvent event) {
                action.actionPerform(null);
            }
        });
    }

    protected void initUserGroupDs(Map<String, Object> params) {
        secRole = (Role) params.get("secRole");
        if (secRole != null) {
            Map<String, Object> parameters = new HashMap<String, Object>();
            parameters.put("secRole", secRole);
            userGroupsDs.refresh(parameters);
        }
    }

    protected void processSelectedItems(Set<Entity> selectedItems) {
        hasInActiveUser = false;
        Set<UserGroup> userGroups = new HashSet<UserGroup>();
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
