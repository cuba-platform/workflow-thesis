/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 18.01.11 12:57
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.HierarchicalDatasourceImpl;
import com.haulmont.workflow.core.entity.DesignLocKey;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class DesignLocKeyDatasource extends HierarchicalDatasourceImpl<DesignLocKey, UUID> {

    public DesignLocKeyDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
        super(context, dataservice, id, metaClass, viewName);
    }

    @Override
    protected void loadData(Map<String, Object> params) {
        for (Object entity : data.values()) {
            detachListener((Instance) entity);
        }
        data.clear();

        Collection<DesignLocKey> allKeys = (Collection<DesignLocKey>) params.get("keysCollection");

        for (DesignLocKey designLocKey : allKeys) {
            data.put(designLocKey.getId(), designLocKey);
            attachListener(designLocKey);
        }
    }

}
