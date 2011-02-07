/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.12.10 16:11
 *
 * $Id$
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity(name = "wf$Design")
@Table(name = "WF_DESIGN")
@NamePattern("%s|name")
public class Design extends StandardEntity {

    private static final long serialVersionUID = 4822453489596189614L;

    @Column(name = "NAME", length = 100)
    private String name;

    @Column(name = "SRC")
    private String src;

    @Column(name = "NOTIFICATION_MATRIX")
    private byte[] notificationMatrix;

    @Column(name = "NOTIFICATION_MATRIX_UPLOADED")
    private Boolean notificationMatrixUploaded;

    @Column(name = "LOCALIZATION")
    private String localization;

    @OneToMany(mappedBy = "design", fetch = FetchType.LAZY)
    @OnDelete(DeletePolicy.CASCADE)
    private Set<DesignScript> scripts;

    @Column(name = "COMPILE_TS")
    private Date compileTs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public byte[] getNotificationMatrix() {
        return notificationMatrix;
    }

    public void setNotificationMatrix(byte[] notificationMatrix) {
        this.notificationMatrix = notificationMatrix;
    }

    public Boolean getNotificationMatrixUploaded() {
        return notificationMatrixUploaded;
    }

    public void setNotificationMatrixUploaded(Boolean notificationMatrixUploaded) {
        this.notificationMatrixUploaded = notificationMatrixUploaded;
    }

    public String getLocalization() {
        return localization;
    }

    public void setLocalization(String localization) {
        this.localization = localization;
    }

    public Set<DesignScript> getScripts() {
        return scripts;
    }

    public void setScripts(Set<DesignScript> scripts) {
        this.scripts = scripts;
    }

    public Date getCompileTs() {
        return compileTs;
    }

    public void setCompileTs(Date compileTs) {
        this.compileTs = compileTs;
    }
}
