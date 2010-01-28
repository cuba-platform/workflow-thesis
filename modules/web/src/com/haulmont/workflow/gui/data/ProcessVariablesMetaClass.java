/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.2010 16:40:55
 *
 * $Id$
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaModel;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.MetaPropertyPath;

import java.util.*;

public class ProcessVariablesMetaClass implements MetaClass {

    private Map<String, MetaProperty> properties = new HashMap<String, MetaProperty>();

    public void addProperty(MetaProperty property) {
        properties.put(property.getName(), property);
    }

    public void setProperties(List<MetaProperty> properties) {
        for (MetaProperty property : properties) {
            this.properties.put(property.getName(), property);
        }
    }

    public MetaModel getModel() {
        return null;
    }

    public Class getJavaClass() {
        return null;
    }

    public MetaProperty getProperty(String name) {
        return null;
    }

    public MetaPropertyPath getPropertyEx(String propertyPath) {
        return new MetaPropertyPath(this, properties.get(propertyPath));
    }

    public Collection<MetaProperty> getOwnProperties() {
        return null;
    }

    public Collection<MetaProperty> getProperties() {
        return null;
    }

    public <T> T createInstance() throws InstantiationException, IllegalAccessException {
        return null;
    }

    public <T> T createInstance(Class<T> clazz) {
        return null;
    }

    public MetaClass getAncestor() {
        return null;
    }

    public Collection<MetaClass> getAncestors() {
        return null;
    }

    public Collection<MetaClass> getDescendants() {
        return null;
    }

    public String getName() {
        return null;
    }

    public String getFullName() {
        return null;
    }

    public String getCaption() {
        return null;
    }

    public String getDescription() {
        return null;
    }

    public UUID getUUID() {
        return null;
    }

    public Map<String, Object> getAnnotations() {
        return null;
    }
}
