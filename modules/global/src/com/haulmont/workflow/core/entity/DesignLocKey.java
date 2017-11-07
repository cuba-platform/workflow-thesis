/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import java.util.Set;

@MetaClass(name = "wf$DesignLocKey")
@NamePattern("%s|key")
@SystemLevel
public class DesignLocKey extends AbstractNotPersistentEntity implements Comparable<DesignLocKey> {

    private static final long serialVersionUID = -4717104012289213679L;

    @MetaProperty
    protected String key;

    @MetaProperty
    protected String caption;

    @MetaProperty
    protected DesignLocKey parentKey;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCaption() {
        return caption == null ? getKey() : caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public DesignLocKey getParentKey() {
        return parentKey;
    }

    public void setParentKey(DesignLocKey parentKey) {
        this.parentKey = parentKey;
    }

    @Override
    public int compareTo(DesignLocKey o) {
        if (getCaption() == null)
            return -1;
        if (o.getCaption() == null)
            return 1;
        return getCaption().compareTo(o.getCaption());
    }

    public String getName() {
        String path = "";
        if (parentKey != null)
            path += parentKey.getName() + ".";
        return path + key;
    }
}