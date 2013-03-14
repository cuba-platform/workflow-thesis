/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.gui.data;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaModel;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.chile.core.model.impl.DatatypeRange;
import com.haulmont.chile.core.model.impl.MetadataObjectImpl;

import java.lang.reflect.AnnotatedElement;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ProcessVariableMetaProperty extends MetadataObjectImpl<MetaProperty> implements MetaProperty {

    private MetaClass metaClass;
    private Range range;

    public ProcessVariableMetaProperty(MetaClass metaClass, String name, Class javaClass) {
        this.metaClass = metaClass;
        this.name = name;
        this.range = new DatatypeRange(Datatypes.getNN(javaClass));
    }

    @Override
    public MetaModel getModel() {
        return metaClass.getModel();
    }

    @Override
    public MetaClass getDomain() {
        return metaClass;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public MetaProperty getInverse() {
        return null;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return null;
    }

    @Override
    public Class<?> getJavaType() {
        return null;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return null;
    }
}
