/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

@MetaClass(name = "wf$DesignLocValue")
@SystemLevel
public class DesignLocValue extends AbstractNotPersistentEntity implements Comparable<DesignLocValue> {

    private static final long serialVersionUID = -999308806151427161L;

    @MetaProperty
    protected String lang;

    @MetaProperty
    protected String message;

    @MetaProperty
    protected Boolean fakeProperty;

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getFakeProperty() {
        return fakeProperty;
    }

    public void setFakeProperty(Boolean property) {
        this.fakeProperty = property;
    }

    @Override
    public int compareTo(DesignLocValue o) {
        if (lang == null)
            return -1;
        if (o.lang == null)
            return 1;
        return lang.compareTo(o.lang);
    }
}