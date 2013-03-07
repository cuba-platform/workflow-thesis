/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.model.*;
import com.haulmont.chile.core.model.impl.MetadataObjectImpl;

import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ProcessVariablesMetaClass extends MetadataObjectImpl<MetaClass> implements MetaClass {

    private Map<String, MetaProperty> properties = new HashMap<String, MetaProperty>();

    public void addProperty(MetaProperty property) {
        properties.put(property.getName(), property);
    }

    public void setProperties(List<MetaProperty> properties) {
        for (MetaProperty property : properties) {
            this.properties.put(property.getName(), property);
        }
    }

    @Override
    public MetaModel getModel() {
        return null;
    }

    @Override
    public Class getJavaClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MetaProperty getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public MetaProperty getPropertyNN(String name) {
        MetaProperty property = getProperty(name);
        if (property == null)
            throw new IllegalArgumentException("Property '" + name + "' not found in " + getName());
        return property;
    }

    @Override
    public MetaPropertyPath getPropertyEx(String propertyPath) {
        return new MetaPropertyPath(this, properties.get(propertyPath));
    }

    @Override
    public MetaPropertyPath getPropertyPath(String propertyPath) {
        return new MetaPropertyPath(this, properties.get(propertyPath));
    }

    @Override
    public Collection<MetaProperty> getOwnProperties() {
        return properties.values();
    }

    @Override
    public Collection<MetaProperty> getProperties() {
        return properties.values();
    }

    @Override
    public <T> T createInstance() throws InstantiationException, IllegalAccessException {
        throw new UnsupportedOperationException();
    }
}