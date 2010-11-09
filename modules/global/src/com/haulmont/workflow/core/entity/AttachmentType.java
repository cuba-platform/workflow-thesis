/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 28.10.2010 11:36:13
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.global.MessageProvider;

import javax.persistence.*;

@Entity(name = "wf$AttachmentType")
@Table(name = "WF_ATTACHMENTTYPE")
@NamePattern("%s|locName")
public class AttachmentType extends StandardEntity implements SoftDelete {
    private static final long serialVersionUID = -7892781327440916914L;

    @Column(name = "NAME", length = 500)
    private String name;

    @Column(name = "COMMENT", length = 1000)
    private String comment;

    //Localization code
    @Column(name = "CODE", length = 200)
    private String code;

    @Column(name = "ISDEFAULT")
    private Boolean isDefault;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @MetaProperty
    public String getLocName() {
        if (this.code != null)
            return MessageProvider.getMessage(getClass(), this.code);
        else
            return name;
    }
}
