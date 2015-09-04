/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.app.core.file.FileDownloadHelper;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.workflow.core.entity.Attachment;

/**
 * @author novikov
 * @version $Id$
 */
public class AttachmentColumnGeneratorHelper {

    public static void addSizeGeneratedColumn(final Table attachmentsTable) {
        if (attachmentsTable.getDatasource().getState() != Datasource.State.VALID) {
            //noinspection unchecked
            attachmentsTable.getDatasource().addStateChangeListener(new Datasource.StateChangeListener() {
                private boolean generatorAdded = false;

                @Override
                public void stateChanged(Datasource.StateChangeEvent e) {
                    if (e.getState() == Datasource.State.VALID && !generatorAdded) {
                        generatorAdded = true;
                        setGenerateColumn(attachmentsTable);
                    }
                }
            });
        } else {
            setGenerateColumn(attachmentsTable);
        }
    }

    private static void setGenerateColumn(final Table attachmentsTable) {
        attachmentsTable.addGeneratedColumn("file.size", new Table.ColumnGenerator<Attachment>() {
            @Override
            public Component generateCell(Attachment attachment) {
                if (attachment != null) {
                    String formattedSize = attachment.getFile() != null ? FileDownloadHelper.formatFileSize(attachment.getFile().getSize()) : "";
                    ComponentsFactory componentsFactory = AppBeans.get(ComponentsFactory.class);
                    Label label = componentsFactory.createComponent(Label.class);
                    label.setValue(formattedSize);
                    return label;
                }
                return null;
            }
        });
    }
}
