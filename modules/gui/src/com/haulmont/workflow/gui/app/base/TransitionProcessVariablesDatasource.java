/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.workflow.core.entity.Card;
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
        Map<String, Class> map = new HashMap<>();
        map.put("dueDate", Date.class);
        map.put("refusedOnly", Boolean.class);
        return map;
    }

    @Override
    protected Card resolveCard(Map<String, Object> params) {
        Card card = (Card) params.get("procContextCard");
        if (card == null)
            return super.resolveCard(params);
        return card;
    }
}
