/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.common.ValueListener;
import com.haulmont.chile.core.common.compatibility.InstancePropertyChangeListenerWrapper;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.UuidProvider;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.ref.WeakReference;
import java.util.*;

public class ProcessVariablesEntity implements Entity {

    private static final long serialVersionUID = -8913144744993978336L;

    protected MetaClass metaClass;
    protected UUID id;
    protected Map<String, Object> values;
    protected Map<String, Object> changed = new HashMap<>();

    protected Collection<WeakReference<PropertyChangeListener>> __valueListeners;

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
    public MetaClass getMetaClass() {
        return metaClass;
    }

    @Override
    public String getInstanceName() {
        return null;
    }

    @Override
    public void addListener(com.haulmont.chile.core.common.ValueListener listener) {
        addPropertyChangeListener(new InstancePropertyChangeListenerWrapper(listener));
    }

    @Override
    public void removeListener(ValueListener listener) {
        removePropertyChangeListener(new InstancePropertyChangeListenerWrapper(listener));
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (__valueListeners == null) {
            __valueListeners = new ArrayList<>();
        }
        __valueListeners.add(new WeakReference<>(listener));
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (__valueListeners != null) {
            for (Iterator<WeakReference<PropertyChangeListener>> it = __valueListeners.iterator(); it.hasNext(); ) {
                PropertyChangeListener iteratorListener = it.next().get();
                if (iteratorListener == null || iteratorListener.equals(listener)) {
                    it.remove();
                }
            }
        }
    }

    @Override
    public void removeAllListeners() {
        if (__valueListeners != null) {
            __valueListeners.clear();
        }
    }

    protected void propertyChanged(String s, Object prev, Object curr) {
        if (__valueListeners != null) {
            for (WeakReference<PropertyChangeListener> reference : new ArrayList<>(__valueListeners)) {
                PropertyChangeListener listener = reference.get();
                if (listener == null) {
                    __valueListeners.remove(reference);
                } else {
                    listener.propertyChanged(new PropertyChangeEvent(this, s, prev, curr));
                }
            }
        }
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

            propertyChanged(name, oldValue, value);
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

            propertyChanged(propertyPath, oldValue, value);
        }
    }

    public Map<String, Object> getChanged() {
        return changed;
    }
}