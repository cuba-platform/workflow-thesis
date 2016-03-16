/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.usergroup;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.UserGroup;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 */
public class UserGroupsDatasource extends CollectionDatasourceImpl<StandardEntity, UUID> {

    @Override
    protected void loadData(Map<String, Object> params) {
        data.clear();
        Role secRole = (Role)params.get("secRole");
        String requiredText = (String)params.get("requiredText");
        HashSet list = (HashSet)params.get("selectedItems");
        List<User> users = loadUsers(secRole);

        List<UserGroup> userGroups = loadUserGroups();

        for (UserGroup userGroup : userGroups) {
            if (StringUtils.isBlank(requiredText) || StringUtils.containsIgnoreCase(userGroup.getInstanceName(), requiredText))
                data.put(userGroup.getId(), userGroup);
        }
        for (User user : users) {
            if (StringUtils.isBlank(requiredText) || StringUtils.containsIgnoreCase(user.getInstanceName(), requiredText))
                data.put(user.getId(), user);
        }
        if (list != null) {
            for (Object obj : list) {
                UUID uuid = ((Entity)obj).getUuid();
                if (!data.containsKey(uuid))
                    data.put(uuid, obj);
            }
        }
    }

    protected List<User> loadUsers(@Nullable Role secRole) {
        LoadContext.Query query;
        if (secRole != null) {
            query = new LoadContext.Query("select u from sec$User u join u.userRoles ur " +
                    "where u.active = true and ur.role.id = :secRole order by u.name")
                    .setParameter("secRole", secRole);
        } else {
            query = new LoadContext.Query("select u from sec$User u where u.active = true order by u.name");
        }
        return dataSupplier.loadList(new LoadContext(User.class)
                .setView("usergroup-add")
                .setQuery(query));
    }

    protected List<UserGroup> loadUserGroups(){
        LoadContext ctx = new LoadContext(UserGroup.class).setView("add");
        ctx.setQueryString("select ug from wf$UserGroup ug order by ug.name");
        return dataSupplier.loadList(ctx);
    }
}
