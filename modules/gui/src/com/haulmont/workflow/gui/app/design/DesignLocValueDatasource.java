/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.DesignLocValue;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignLocValueDatasource extends CollectionDatasourceImpl<DesignLocValue, UUID> {

    @Override
    protected void loadData(Map<String, Object> params) {
        for (Object entity : data.values()) {
            detachListener((Instance) entity);
        }
        data.clear();

        Collection<DesignLocValue> allValues = (Collection<DesignLocValue>) params.get("valuesCollection");
        if (allValues == null)
            return;

        for (DesignLocValue designLocValue : allValues) {
            data.put(designLocValue.getId(), designLocValue);
            attachListener(designLocValue);
        }
    }
}
