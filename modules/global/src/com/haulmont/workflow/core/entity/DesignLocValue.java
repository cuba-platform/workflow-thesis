/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 18.01.11 12:48
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.AbstractNotPersistentEntity;

@MetaClass(name = "wf$DesignLocValue")
public class DesignLocValue extends AbstractNotPersistentEntity implements Comparable<DesignLocValue> {

    private static final long serialVersionUID = -999308806151427161L;

    @MetaProperty
    private String lang;

    @MetaProperty
    private String message;

    @MetaProperty
    private Boolean fakeProperty;

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

    public int compareTo(DesignLocValue o) {
        if (lang == null)
            return -1;
        if (o.lang == null)
            return 1;
        return lang.compareTo(o.lang);
    }
}
