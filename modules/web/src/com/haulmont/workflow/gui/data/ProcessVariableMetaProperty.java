/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 27.01.2010 16:41:13
 *
 * $Id$
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaModel;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.chile.core.model.impl.DatatypeRange;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class ProcessVariableMetaProperty implements MetaProperty {

    private MetaClass metaClass;
    private String name;
    private Range range;

    public ProcessVariableMetaProperty(MetaClass metaClass, String name, Class javaClass) {
        this.metaClass = metaClass;
        this.name = name;
        this.range = new DatatypeRange(Datatypes.getInstance().get(javaClass));
    }

    public MetaModel getModel() {
        return metaClass.getModel();
    }

    public MetaClass getDomain() {
        return metaClass;
    }

    public Range getRange() {
        return range;
    }

    public Type getType() {
        return null;
    }

    public boolean isMandatory() {
        return false;
    }

    public boolean isReadOnly() {
        return false;
    }

    public MetaProperty getInverse() {
        return null;
    }

    public AnnotatedElement getAnnotatedElement() {
        return null;
    }

    public Class<?> getJavaType() {
        return null;
    }

    public Class<?> getDeclaringClass() {
        return null;
    }

    public MetaProperty getAncestor() {
        return null;
    }

    public Collection<MetaProperty> getAncestors() {
        return null;
    }

    public Collection<MetaProperty> getDescendants() {
        return null;
    }

    public String getName() {
        return name;
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
