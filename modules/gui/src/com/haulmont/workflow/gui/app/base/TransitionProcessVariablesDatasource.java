/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.workflow.gui.data.ProcessVariablesDatasource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 */
public class TransitionProcessVariablesDatasource extends ProcessVariablesDatasource {

    @Override
    protected Map<String, Class> getVariableTypes() {
        Map<String, Class> map = new HashMap<String, Class>();
        map.put("dueDate", Date.class);
        map.put("refusedOnly", Boolean.class);
        return map;
    }
}
