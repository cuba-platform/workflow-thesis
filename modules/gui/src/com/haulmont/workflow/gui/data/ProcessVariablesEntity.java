/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.common.ValueListener;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.UuidProvider;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

/**
 * @author
 * @version $Id$
 */
public class ProcessVariablesEntity implements Entity, Instance {

    private static final long serialVersionUID = -8913144744993978336L;

    protected MetaClass metaClass;
    protected UUID id;
    protected Map<String, Object> values;
    protected Map<String, Object> changed = new HashMap<>();
    protected Set<ValueListener> listeners = new LinkedHashSet<>();

    public ProcessVariablesEntity(MetaClass metaClass, Map<String, Object> variables) {
        this.metaClass = metaClass;
        this.id = UuidProvider.createUuid();
        this.values = variables;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public UUID getUuid() {
        return id;
    }

    @Override
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public String getInstanceName() {
        return null;
    }

    @Override
    public void addListener(ValueListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(ValueListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }

    @Override
    public <T> T getValue(String name) {
        return (T) values.get(name);
    }

    @Override
    public void setValue(String name, Object value) {
        Object oldValue = values.get(name);
        if (!ObjectUtils.equals(oldValue, value)) {
            values.put(name, value);
            changed.put(name, value);
            for (ValueListener listener : listeners) {
                listener.propertyChanged(this, name, oldValue, value);
            }
        }
    }

    @Override
    public <T> T getValueEx(String propertyPath) {
        return (T) values.get(propertyPath);
    }

    @Override
    public void setValueEx(String propertyPath, Object value) {
        Object oldValue = values.get(propertyPath);
        if (!ObjectUtils.equals(oldValue, value)) {
            values.put(propertyPath, value);
            changed.put(propertyPath, value);
            for (ValueListener listener : listeners) {
                listener.propertyChanged(this, propertyPath, oldValue, value);
            }
        }
    }

    public Map<String, Object> getChanged() {
        return changed;
    }
}