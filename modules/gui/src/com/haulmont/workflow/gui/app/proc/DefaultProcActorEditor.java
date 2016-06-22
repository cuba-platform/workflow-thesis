/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.proc;

import com.google.common.collect.*;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.DefaultProcActor;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author chekashkin
 * @version $Id$
 */
public class DefaultProcActorEditor<T extends DefaultProcActor> extends AbstractEditor<T> {

    @Inject
    protected LookupField sortOrderField;
    @Inject
    protected CollectionDatasource<User, UUID> usersDs;
    @WindowParam
    protected List<UUID> userIds;

    @Override
    public void init(Map<String, Object> params) {
        WindowParams.DISABLE_AUTO_REFRESH.set(params, true);
        super.init(params);
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);
        T defaultProcActor = getItem();
        initUsersDs(defaultProcActor);
        if (defaultProcActor.getProcRole().getMultiUser())
            initSortOrderField();
        else
            sortOrderField.setEditable(false);
    }

    protected void initUsersDs(T defaultProcActor) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("userIds", userIds);
        params.put("secRole", defaultProcActor.getProcRole().getRole());
        usersDs.refresh(params);
    }

    protected void initSortOrderField() {
        final T item = getItem();
        int maxSortOrder = getMaxSortOrder();
        if (maxSortOrder == 1)
            sortOrderField.setOptionsList(Lists.newArrayList(1));
        else
            sortOrderField.setOptionsList(Lists.newArrayList(ContiguousSet.create(Range.closed(1, maxSortOrder),
                    DiscreteDomain.integers())));

        sortOrderField.setValue(item.getSortOrder());
        sortOrderField.addListener(new ValueListener() {
            @Override
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                item.setSortOrder((Integer) value);
            }
        });
    }

    protected int getMaxSortOrder() {
        return userIds.size() + 1;
    }
}
