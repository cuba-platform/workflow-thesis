/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.workflow.gui.data.ProcessVariablesDatasource;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Gorbunkov
 * @version $Id$
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
