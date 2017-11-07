/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.entity.Card;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnumCardPropertyHandler extends BaseCardPropertyHandler {


    public EnumCardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        super(clazz, card, useExpression);
    }

    @Override
    public Map<String, Object> loadObjects() {
        Map<String, Object> map = new LinkedHashMap<String, Object>();
        try {
            Enum[] enums = (Enum[]) clazz.getMethod("values").invoke(null);
            Messages messages = AppBeans.get(Messages.NAME);
            for (Enum value : enums) {
                EnumClass enumValue = (EnumClass) value;
                map.put(enumValue.getId().toString(), messages.getMessage(value));
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        }

        return map;
    }
}
