/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 19.01.11 12:37
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "wf$ProcStageType")
@Table(name = "WF_PROC_STAGE_TYPE")
@NamePattern("%s|name")
public class ProcStageType extends StandardEntity {
    private static final long serialVersionUID = -4894528040675569985L;

    @Column(name = "CODE", length = 200)
    private String code;

    @Column(name = "NAME", length = 200)
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}