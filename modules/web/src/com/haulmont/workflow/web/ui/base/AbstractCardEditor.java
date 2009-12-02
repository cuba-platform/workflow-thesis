/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 02.12.2009 9:57:05
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractCardEditor extends AbstractEditor {

    protected Datasource<Card> cardDs;
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected Table attachmentsTable;

    protected CardRolesFrame cardRolesFrame;

    public AbstractCardEditor(IFrame frame) {
        super(frame);
    }

    protected void initFields() {
        cardDs = getDsContext().get("cardDs");
        cardRolesDs = getDsContext().get("cardRolesDs");
        attachmentsTable = getComponent("attachmentsTable");
        cardRolesFrame = getComponent("cardRolesFrame");
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        initFields();

        if (attachmentsTable != null) {
            TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable);
            attachmentsTH.createCreateAction(
                    new ValueProvider() {
                        public Map<String, Object> getValues() {
                            Map<String, Object> values = new HashMap<String, Object>();
                            values.put("card", cardDs.getItem());
                            values.put("file", new FileDescriptor());
                            return values;
                        }
                        public Map<String, Object> getParameters() {
                            return Collections.emptyMap();
                        }
                    },
                    WindowManager.OpenType.DIALOG);
            attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG);
            attachmentsTH.createRemoveAction(false);
        }

        if (cardRolesFrame != null) {
            cardRolesFrame.init();
        }

    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");

        if (cardRolesFrame != null) {
            cardRolesFrame.setCard((Card) getItem());
        }

        final ActionsFrame actionsFrame = getComponent("actionsFrame");

        if (PersistenceHelper.isNew(item)) {
            cardDs.addListener(new DsListenerAdapter<Card>() {
                @Override
                public void valueChanged(Card source, String property, Object prevValue, Object value) {
                    if ("proc".equals(property)) {
                        actionsFrame.initActions(source);
                    }
                }
            });
        } else {
            actionsFrame.initActions((Card) getItem());
        }
    }
}
