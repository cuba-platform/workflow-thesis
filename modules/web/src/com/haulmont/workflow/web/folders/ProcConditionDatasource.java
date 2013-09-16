/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.folders;

import com.haulmont.chile.core.model.Instance;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.ProcCondition;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author pavlov
 * @version $Id$
 */
public class ProcConditionDatasource extends CollectionDatasourceImpl<ProcCondition, UUID> {

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
