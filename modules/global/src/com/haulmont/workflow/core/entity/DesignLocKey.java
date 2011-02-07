/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 18.01.11 12:35
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

import java.util.Set;

@MetaClass(name = "wf$DesignLocKey")
@NamePattern("%s|key")
public class DesignLocKey extends AbstractNotPersistentEntity implements Comparable<DesignLocKey> {

    private static final long serialVersionUID = -4717104012289213679L;

    @MetaProperty
    private String key;

    @MetaProperty
    private String caption;

    @MetaProperty
    private DesignLocKey parentKey;

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
