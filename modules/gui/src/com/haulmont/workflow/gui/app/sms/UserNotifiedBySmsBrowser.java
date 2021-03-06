/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.sms;

import com.haulmont.cuba.client.ClientConfig;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.Metadata;
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
    protected Metadata metadata;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        if (table != null) {
            ClientConfig clientConfig = AppBeans.get(Configuration.class).getConfig(ClientConfig.class);
            table.addAction(new AbstractAction("add", clientConfig.getTableInsertShortcut()) {
                @Override
                public void actionPerform(Component component) {
                    final CollectionDatasource<UserNotifiedBySms, UUID> ds = table.getDatasource();
                    Map<String, Object> params = new HashMap<>();
                    params.put("multiselect", true);
                    params.put("isLookup", true);
                    params.put("hasEmployees", true);
                    openLookup("sec$User.lookup", new Handler() {
                        @SuppressWarnings("unchecked")
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
                                        UserNotifiedBySms userNotifiedBySms = metadata.create(UserNotifiedBySms.class);
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

                @Override
                public String getCaption() {
                    return messages.getMainMessage("actions.Add");
                }
            });
        }
    }
}
