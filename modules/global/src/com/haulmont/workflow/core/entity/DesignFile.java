/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 13:56
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.BaseUuidEntity;

import javax.persistence.*;

@Entity(name = "wf$DesignFile")
@Table(name = "WF_DESIGN_FILE")
public class DesignFile extends BaseUuidEntity {

    private static final long serialVersionUID = -6736255284492025204L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DESIGN_ID")
    private Design design;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "TYPE", length = 20)
    private String type;

    @Column(name = "CONTENT")
    private String content;

    @Column(name = "BINARY_CONTENT")
    private byte[] binaryContent;

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
