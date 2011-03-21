/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 14.12.2010 9:45:58
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Attachment;
import com.vaadin.ui.Component;

import java.text.NumberFormat;
import java.util.UUID;
import java.util.HashMap;

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

    private static void setGenerateColumn(final Table attachmentsTable)
    {
        final HashMap<UUID, com.vaadin.ui.Component> map = new HashMap<UUID, com.vaadin.ui.Component>();
        ((com.vaadin.ui.Table) WebComponentsHelper.unwrap(attachmentsTable)).addGeneratedColumn(
                attachmentsTable.getDatasource().getMetaClass().getPropertyEx("file.size"),
                new com.vaadin.ui.Table.ColumnGenerator() {
                    public Component generateCell(com.vaadin.ui.Table table, Object itemId, Object columnId) {
                        UUID uuid = (UUID) itemId;
                        if (map.containsKey(uuid)) {
                            return map.get(uuid);
                        }
                        Attachment attach = (Attachment) attachmentsTable.getDatasource().getItem(uuid);
                        com.vaadin.ui.Label label = new com.vaadin.ui.Label(formatSize(attach.getFile().getSize(), 0));
                        label.setWidth("-1px");
                        map.put(uuid, label);
                        return label;
                    }
                });
    }

    private static String formatSize(long longSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
            fmt.setMaximumFractionDigits(decimalPos);
        }
        final double size = longSize;
        double val = size / (1024 * 1024);
        if (val > 1) {
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtMb"));
        }
        val = size / 1024;
        if (val > 10) {
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtKb"));
        }
        return fmt.format(size).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB"));
    }
}
