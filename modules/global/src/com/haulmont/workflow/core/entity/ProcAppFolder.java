/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.entity;

import com.haulmont.cuba.core.entity.AbstractSearchFolder;
import com.haulmont.cuba.core.entity.AppFolder;

import javax.persistence.*;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
@Entity(name = "wf$ProcAppFolder")
@Table(name = "WF_PROC_APP_FOLDER")
@PrimaryKeyJoinColumn(name = "FOLDER_ID", referencedColumnName = "FOLDER_ID")
@DiscriminatorValue("P")
public class ProcAppFolder extends AppFolder {
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
