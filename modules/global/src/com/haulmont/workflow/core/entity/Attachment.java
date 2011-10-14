/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 13.11.2009 11:46:25
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDeleteInverse;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "wf$Attachment")
@Table(name = "WF_ATTACHMENT")
@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("-")
@SystemLevel
public class Attachment extends StandardEntity {

    private static final long serialVersionUID = 8954537950047549199L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FILE_ID")
    protected FileDescriptor file;

    @Column(name = "NAME", length = 500)
    protected String name;

    @Column(name = "COMMENT", length = 1000)
    protected String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TYPE_ID")
    @OnDeleteInverse(value = DeletePolicy.DENY)
    protected AttachmentType attachType;

    @Column(name = "SIGNATURES")
    protected String signatures;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "VERSION_OF_ID")
    protected Attachment versionOf;

    @Column(name = "VERSION_NUM")
    protected Integer versionNum;

    public FileDescriptor getFile() {
        return file;
    }

    public void setFile(FileDescriptor file) {
        this.file = file;
    }

    public AttachmentType getAttachType() {
        return attachType;
    }

    public void setAttachType(AttachmentType attachType) {
        this.attachType = attachType;
    }

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

    public String getSignatures() {
        return signatures;
    }

    public void setSignatures(String signatures) {
        this.signatures = signatures;
    }

    @MetaProperty
    public String getClassName() {
        return this.getClass().getSimpleName();
    }

    public Attachment getVersionOf() {
        return versionOf;
    }

    public void setVersionOf(Attachment versionOf) {
        this.versionOf = versionOf;
    }

    public Integer getVersionNum() {
        return versionNum;
    }

    public void setVersionNum(Integer versionNum) {
        this.versionNum = versionNum;
    }
}
