/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.entity;

import com.haulmont.chile.core.annotations.NamePattern;
import com.haulmont.cuba.core.entity.StandardEntity;
import com.haulmont.cuba.core.entity.annotation.OnDelete;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;
import com.haulmont.cuba.core.global.DeletePolicy;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity(name = "wf$Design")
@Table(name = "WF_DESIGN")
@NamePattern("%s|name")
@SystemLevel
public class Design extends StandardEntity {

    private static final long serialVersionUID = 4822453489596189614L;

    @Column(name = "NAME", length = 100)
    protected String name;

    @Lob
    @Column(name = "SRC", length = 0)
    protected String src;

    @Column(name = "DESIGN_TYPE")
    protected String type = DesignType.COMMON.getId();;

    @Column(name = "NOTIFICATION_MATRIX")
    protected byte[] notificationMatrix;

    @Column(name = "NOTIFICATION_MATRIX_UPLOADED")
    protected Boolean notificationMatrixUploaded;

    @Lob
    @Column(name = "LOCALIZATION")
    protected String localization;

    @OneToMany(mappedBy = "design", fetch = FetchType.LAZY)
    @OnDelete(DeletePolicy.CASCADE)
    protected Set<DesignScript> scripts;

    @OneToMany(mappedBy = "design", fetch = FetchType.LAZY)
    @OnDelete(DeletePolicy.CASCADE)
    private Set<DesignProcessVariable> designProcessVariables;

    @OneToMany(mappedBy = "design", fetch = FetchType.LAZY)
    @OnDelete(DeletePolicy.CASCADE)
    private List<DesignFile> designFiles;

    @Column(name = "COMPILE_TS")
    protected Date compileTs;

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

    public DesignType getType() {
        return DesignType.fromId(type);
    }

    public void setType(DesignType type) {
        this.type = type == null ? null : type.getId();
    }

    public Set<DesignProcessVariable> getDesignProcessVariables() {
        return designProcessVariables;
    }

    public void setDesignProcessVariables(Set<DesignProcessVariable> designProcessVariables) {
        this.designProcessVariables = designProcessVariables;
    }

    public List<DesignFile> getDesignFiles() {
        return designFiles;
    }

    public void setDesignFiles(List<DesignFile> designFiles) {
        this.designFiles = designFiles;
    }
}
