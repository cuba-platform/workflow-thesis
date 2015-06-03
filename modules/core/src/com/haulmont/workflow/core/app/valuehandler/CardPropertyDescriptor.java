/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.valuehandler;

import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.global.WfEntityDescriptor;

/**
 * @author zaharchenko
 * @version $Id$
 */
public class CardPropertyDescriptor implements WfEntityDescriptor {

    private String value;
    private AttributeType attributeType;
    private String metaClassName;

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public AttributeType getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(AttributeType attributeType) {
        this.attributeType = attributeType;
    }

    @Override
    public String getMetaClassName() {
        return metaClassName;
    }

    public void setMetaClassName(String metaClassName) {
        this.metaClassName = metaClassName;
    }
}
