/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.entity.AppFolder;
import com.haulmont.cuba.core.entity.annotation.Extends;

import javax.persistence.*;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
@Entity(name = "wf$ProcAppFolder")
@Table(name = "WF_PROC_APP_FOLDER")
@PrimaryKeyJoinColumn(name = "FOLDER_ID", referencedColumnName = "ID")
@DiscriminatorValue("P")
@Extends(AppFolder.class)
public class ProcAppFolder extends AppFolder {

    private static final long serialVersionUID = 2716906222330698569L;

    @Column(name = "PROC_CONDITIONS_XML")
    protected String procAppFolderXml;

    @Override
    public void copyFrom(AbstractSearchFolder srcFolder) {
        super.copyFrom(srcFolder);

        setProcAppFolderXml(((ProcAppFolder) srcFolder).getProcAppFolderXml());
    }

    public String getProcAppFolderXml() {
        return procAppFolderXml;
    }

    public void setProcAppFolderXml(String procAppFolderXml) {
        this.procAppFolderXml = procAppFolderXml;
    }
}
