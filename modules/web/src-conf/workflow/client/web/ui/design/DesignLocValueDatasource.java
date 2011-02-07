/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 18.01.11 16:21
 *
 * $Id$
 */
package workflow.client.web.ui.design;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.DesignLocValue;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class DesignLocValueDatasource extends CollectionDatasourceImpl<DesignLocValue, UUID> {

    public DesignLocValueDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
        super(context, dataservice, id, metaClass, viewName);
    }

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
