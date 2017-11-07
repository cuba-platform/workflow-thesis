/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.enums.AttributeType;

import java.io.Serializable;
import java.util.Map;

public abstract class CardPropertyHandler implements Serializable {

    protected Class clazz;

    protected Card card;

    protected Boolean useExpression = false;

    public CardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        this.clazz = clazz;
        this.card = card;
        this.useExpression = useExpression;
    }

    public abstract String getLocalizedValue(String value);

    public abstract Object getValue(String value);

    public abstract Map<String, Object> loadObjects();

    public abstract AttributeType getAttributeType();

    public abstract String getMetaClassName();
}
