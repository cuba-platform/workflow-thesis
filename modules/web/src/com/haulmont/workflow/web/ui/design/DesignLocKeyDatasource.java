/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.gui.data.impl.HierarchicalDatasourceImpl;
import com.haulmont.workflow.core.entity.DesignLocKey;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignLocKeyDatasource extends HierarchicalDatasourceImpl<DesignLocKey, UUID> {

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
