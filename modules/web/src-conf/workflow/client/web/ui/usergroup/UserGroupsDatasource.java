/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 06.08.2010 17:36:47
 *
 * $Id$
 */
package workflow.client.web.ui.usergroup;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.UserGroup;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserGroupsDatasource extends CollectionDatasourceImpl<StandardEntity, UUID> {
    public UserGroupsDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
        super(context, dataservice, id, metaClass, viewName);
    }

    @Override
    protected void loadData(Map<String, Object> params) {
        data.clear();
        String queryString = "";
        Role secRole = (Role)params.get("secRole");
        if (secRole != null) {
          queryString = "select u from sec$User u join u.userRoles ur where ur.role.id = :secRole order by u.name";
        } else {
            queryString = "select u from sec$User u order by u.name";
        }

        LoadContext ctx = new LoadContext(User.class).setView("usergroup-add");
        LoadContext.Query query = ctx.setQueryString(queryString);
        query.addParameter("secRole", secRole);

        List<User> users = dataservice.loadList(ctx);

        ctx = new LoadContext(UserGroup.class).setView("add");
        ctx.setQueryString("select ug from wf$UserGroup ug order by ug.name");

        List<UserGroup> userGroups = dataservice.loadList(ctx);

        for (User user : users) {
            data.put(user.getId(), user);
        }
        for (UserGroup userGroup : userGroups) {
            data.put(userGroup.getId(), userGroup);
        }

//        State prevState = state;
//        valid();
//        forceStateChanged(prevState);
    }
}
