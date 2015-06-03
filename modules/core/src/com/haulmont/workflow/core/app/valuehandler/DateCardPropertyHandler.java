/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.workflow.core.entity.Card;
import org.ocpsoft.prettytime.nlp.PrettyTimeParser;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zaharchenko
 * @version $Id$
 */
public class DateCardPropertyHandler extends BaseCardPropertyHandler {

    private static Map<String, String> REPLACEMENT = new LinkedHashMap<String, String>() {
        {
            put("current time", "now");
            put("current date", "now");
            put("current", "now");
            put("+", "plus");
            put("-", "minus");
            put("previous", "last");
        }
    };

    public DateCardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        super(clazz, card, useExpression);
    }

    protected Object evaluateExpression(String value) {

        String formattedValue = " " + value.toLowerCase().replaceAll("\\s+", " ") + " ";
        for (String key : REPLACEMENT.keySet()) {
            formattedValue = formattedValue.replace(" " + key + " ", " " + REPLACEMENT.get(key) + " ");
        }
        formattedValue = formattedValue.trim();
        List<Date> dates = new PrettyTimeParser().parse(formattedValue);
        if (dates.isEmpty()) {
            return null;
        }
        if (formattedValue.contains(" minus ") && dates.size() == 2) {
            Long time1 = dates.get(0).getTime();
            Long time2 = dates.get(1).getTime();
            Long difference = time2 - time1;
            Long resultTime = time1 - difference;
            return new Date(resultTime);
        }
        return dates.get(dates.size() - 1);
    }
}
