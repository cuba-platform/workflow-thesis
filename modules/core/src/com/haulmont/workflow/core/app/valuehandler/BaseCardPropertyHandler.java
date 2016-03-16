/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;


import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.workflow.core.app.WfEntityDescriptorTools;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.global.CardPropertyUtils;
import org.apache.commons.lang.BooleanUtils;

import java.util.HashMap;
import java.util.Map;

/**
 */
public class BaseCardPropertyHandler extends CardPropertyHandler {

    protected WfEntityDescriptorTools wfEntityDescriptorTools;

    public BaseCardPropertyHandler(Class clazz, Card card, Boolean useExpression) {
        super(clazz, card, useExpression);
        wfEntityDescriptorTools = AppBeans.get(WfEntityDescriptorTools.NAME);
    }

    @Override
    public String getLocalizedValue(String value) {
        CardPropertyDescriptor attributeValue = prepareDebtorPropertyDescriptor(clazz, value);
        return wfEntityDescriptorTools.getLocalizedValue(attributeValue);
    }

    @Override
    public Object getValue(String value) {
        CardPropertyDescriptor attributeValue = prepareDebtorPropertyDescriptor(clazz, value);
        return wfEntityDescriptorTools.getValue(attributeValue);

    }

    Object evaluateExpression(String value) {
        return value;
    }

    @Override
    public Map<String, Object> loadObjects() {
        return new HashMap<>();
    }

    @Override
    public AttributeType getAttributeType() {
        CardPropertyDescriptor attributeValue = prepareDebtorPropertyDescriptor(clazz, null);
        return attributeValue.getAttributeType();
    }

    @Override
    public String getMetaClassName() {
        CardPropertyDescriptor attributeValue = prepareDebtorPropertyDescriptor(clazz, null);
        return attributeValue.getMetaClassName();
    }

    protected CardPropertyDescriptor prepareDebtorPropertyDescriptor(Class clazz, String value) {
        AttributeType attributeType = CardPropertyUtils.getAttributeTypeFromClass(clazz);
        CardPropertyDescriptor entityAttribute = new CardPropertyDescriptor();
        entityAttribute.setAttributeType(attributeType);
        if (AttributeType.ENUM == attributeType) {
            entityAttribute.setMetaClassName(clazz.getName());
        }
        if (AttributeType.ENTITY == attributeType) {
            Metadata metadata = AppBeans.get(Metadata.NAME);
            com.haulmont.chile.core.model.MetaClass propertyMetaClass = metadata.getSession().getClass(clazz);
            entityAttribute.setMetaClassName(propertyMetaClass.getName());
        }
        if ("null".equals(value)) {
            value = null;
        }
        if (BooleanUtils.isTrue(useExpression) && value != null) {
            Object evaluatedValue = evaluateExpression(value);
            entityAttribute.setValue(wfEntityDescriptorTools.getStringValue(evaluatedValue));
        } else {
            entityAttribute.setValue(value);
        }
        return entityAttribute;
    }
}
