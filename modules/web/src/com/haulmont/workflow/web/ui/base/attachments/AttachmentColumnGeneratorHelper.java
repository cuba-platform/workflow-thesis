/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Attachment;
import com.vaadin.ui.Component;

import java.util.HashMap;
import java.util.UUID;

public class AttachmentColumnGeneratorHelper {

    public static void addSizeGeneratedColumn(final Table attachmentsTable) {
        if (attachmentsTable.getDatasource().getState() != Datasource.State.VALID) {
            attachmentsTable.getDatasource().addListener(new DsListenerAdapter() {
                private boolean generatorAdded = false;

                @Override
                public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                    super.stateChanged(ds, prevState, state);
                    if (state == Datasource.State.VALID && !generatorAdded) {
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
        final HashMap<UUID, com.vaadin.ui.Component> map = new HashMap<UUID, com.vaadin.ui.Component>();
        ((com.vaadin.ui.Table) WebComponentsHelper.unwrap(attachmentsTable)).addGeneratedColumn(
                attachmentsTable.getDatasource().getMetaClass().getPropertyPath("file.size"),
                new com.vaadin.ui.Table.ColumnGenerator() {
                    @Override
                    public Component generateCell(com.vaadin.ui.Table table, Object itemId, Object columnId) {
                        UUID uuid = (UUID) itemId;
                        if (map.containsKey(uuid)) {
                            return map.get(uuid);
                        }
                        Attachment attach = (Attachment) attachmentsTable.getDatasource().getItem(uuid);
                        String formattedSize = FileDownloadHelper.formatFileSize(attach.getFile().getSize());
                        com.vaadin.ui.Label label = new com.vaadin.ui.Label(formattedSize);
                        label.setWidth("-1px");
                        map.put(uuid, label);
                        return label;
                    }
                });
    }
}
