/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.SoftDelete;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.MessageProvider;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;

@Entity(name = "wf$AttachmentType")
@Table(name = "WF_ATTACHMENTTYPE")
@NamePattern("%s|locName")
@SystemLevel
public class AttachmentType extends StandardEntity implements SoftDelete {
    private static final long serialVersionUID = -7892781327440916914L;

    @Column(name = "NAME", length = 500)
    protected String name;

    @Column(name = "ATTACHMENTTYPE_COMMENT", length = 1000)
    protected String comment;

    //Localization code
    @Column(name = "CODE", length = 200)
    protected String code;

    @Column(name = "ISDEFAULT")
    protected Boolean isDefault = false;

    @Column(name = "ISSYSTEM")
    protected Boolean isSystem = false;

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
        if (StringUtils.isNotBlank(this.name))
            return name;
        else
            return MessageProvider.getMessage(getClass(), this.code);
    }

    public Boolean getIsSystem() {
        return isSystem;
    }
}
