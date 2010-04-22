/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 20.04.2010 13:23:37
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.gui.data.DataService;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.workflow.gui.data.ProcessVariablesDatasource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransitionProcessVariablesDatasource extends ProcessVariablesDatasource{
    public TransitionProcessVariablesDatasource(DsContext dsContext, DataService dataservice, String id, MetaClass metaClass, String viewName) {
        super(dsContext, dataservice, id, metaClass, viewName);
    }

    @Override
    protected Map<String, Class> getVariableTypes() {
        Map<String, Class> map = new HashMap<String, Class>();
        map.put("dueDate", Date.class);
        return map;

    }
}
