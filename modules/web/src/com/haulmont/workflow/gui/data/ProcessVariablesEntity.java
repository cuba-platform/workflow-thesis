/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.2010 12:28:48
 *
 * $Id$
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.common.ValueListener;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.UuidProvider;
import org.apache.commons.lang.ObjectUtils;

import java.util.*;

public class ProcessVariablesEntity implements Entity, Instance {

    private static final long serialVersionUID = -8913144744993978336L;

    private MetaClass metaClass;
    private UUID id;
    private Map<String, Object> values;
    private Map<String, Object> changed = new HashMap<String, Object>();
    private Set<ValueListener> listeners = new LinkedHashSet<ValueListener>();

    public ProcessVariablesEntity(MetaClass metaClass, Map<String, Object> variables) {
        this.metaClass = metaClass;
        this.id = UuidProvider.createUuid();
        this.values = variables;
    }

    public Object getId() {
        return id;
    }

    public UUID getUuid() {
        return id;
    }

    public MetaClass getMetaClass() {
        return metaClass;
    }

    public String getInstanceName() {
        return null;
    }

    public void addListener(ValueListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ValueListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void removeAllListeners() {
        listeners.clear();
    }

    public <T> T getValue(String name) {
        return (T) values.get(name);
    }

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

    public <T> T getValueEx(String propertyPath) {
        return (T) values.get(propertyPath);
    }

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
