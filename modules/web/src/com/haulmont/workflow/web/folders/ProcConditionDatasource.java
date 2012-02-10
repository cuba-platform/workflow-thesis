/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.folders;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.ProcCondition;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
public class ProcConditionDatasource extends CollectionDatasourceImpl<ProcCondition, UUID> {
    private static final long serialVersionUID = -6377505935643888211L;

    public ProcConditionDatasource(DsContext context, DataService dataservice, String id, MetaClass metaClass, String viewName) {
        super(context, dataservice, id, metaClass, viewName);
    }

    @Override
    protected void loadData(Map<String, Object> params) {
        for (Object entity : data.values()) {
            detachListener((Instance) entity);
        }
        data.clear();

        Collection<ProcCondition> procConditions = (Collection<ProcCondition>) params.get("procConditions");

        if (procConditions != null) {
            for (ProcCondition procCondition : procConditions) {
                data.put(procCondition.getId(), procCondition);
                attachListener(procCondition);
            }
        }
    }
}
