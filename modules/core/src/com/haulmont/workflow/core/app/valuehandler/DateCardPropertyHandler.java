/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.cuba.core.app.prettytime.CubaPrettyTimeParser;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.Card;

/**
 * @author zaharchenko
 * @version $Id$
 */
public class DateCardPropertyHandler extends BaseCardPropertyHandler {

    public DateCardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        super(clazz, card, useExpression);
    }

    protected Object evaluateExpression(String value) {
        CubaPrettyTimeParser prettyTimeParser = AppBeans.get(CubaPrettyTimeParser.NAME);
        return prettyTimeParser.parse(value);
    }
}
