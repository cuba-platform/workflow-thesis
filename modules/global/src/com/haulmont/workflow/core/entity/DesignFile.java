/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

import javax.persistence.*;

@Entity(name = "wf$DesignFile")
@Table(name = "WF_DESIGN_FILE")
@SystemLevel
public class DesignFile extends BaseUuidEntity {

    private static final long serialVersionUID = -6736255284492025204L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    protected Design design;

    @Column(name = "NAME", length = 100)
    protected String name;

    @Column(name = "DESIGN_FILE_TYPE", length = 20)
    protected String type;

    @Column(name = "CONTENT", length = 0)
    protected String content;

    @Column(name = "BINARY_CONTENT")
    protected byte[] binaryContent;

    public Design getDesign() {
        return design;
    }

    public void setDesign(Design design) {
        this.design = design;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getBinaryContent() {
        return binaryContent;
    }

    public void setBinaryContent(byte[] binaryContent) {
        this.binaryContent = binaryContent;
    }
}
