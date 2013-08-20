/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.sms;

import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ListActionType;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.UserNotifiedBySms;

import javax.inject.Inject;
import java.util.*;

/**
 * @author novikov
 * @version $Id$
 */
public class UserNotifiedBySmsBrowser extends AbstractLookup {

    @Inject
    protected Table table;

    @Inject
    protected Button add;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        if (table != null) {
            add.setCaption(messages.getMainMessage("actions.Add"));
            ComponentsHelper.createActions(table, EnumSet.of(ListActionType.REMOVE, ListActionType.REFRESH));
        }
    }

    public void addUser(Component component) {
        final CollectionDatasource<UserNotifiedBySms, UUID> ds = table.getDatasource();
        Map<String, Object> params = new HashMap<>();
        params.put("multiselect", true);
        params.put("isLookup", true);
        params.put("hasEmployees", true);
        openLookup("sec$User.lookup", new com.haulmont.cuba.gui.components.Window.Lookup.Handler() {
            public void handleLookup(Collection items) {
                if (items != null && items.size() > 0) {
                    for (User item : (Collection<User>) items) {
                        boolean containItem = false;
                        for (UUID uuid : ds.getItemIds()) {
                            UserNotifiedBySms userNotifiedBySms = ds.getItem(uuid);
                            if (item.equals(userNotifiedBySms.getUser())) {
                                containItem = true;
                                break;
                            }
                        }
                        if (!containItem) {
                            UserNotifiedBySms userNotifiedBySms = MetadataProvider.create(UserNotifiedBySms.class);
                            userNotifiedBySms.setUser(item);
                            ds.addItem(userNotifiedBySms);
                        }
                    }
                    ds.commit();
                    ds.refresh();
                }
            }
        }, WindowManager.OpenType.THIS_TAB, params);
    }
}
