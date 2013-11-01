/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.gui.base.AbstractWfAccessData;

import javax.inject.Inject;
import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AbstractCardEditor<T extends Card> extends AbstractEditor<T> {

    @Inject
    protected Datasource<T> cardDs;
    @Inject
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    @Inject
    protected Table attachmentsTable;

    @Inject
    protected CardProcFrame cardProcFrame;
    @Inject
    protected CardRolesFrame cardRolesFrame;
    @Inject
    protected ResolutionsFrame resolutionsFrame;
    @Inject
    protected CardAttachmentsFrame cardAttachmentsFrame;

    @Inject
    protected CollectionDatasource<CardAttachment, UUID> attachmentsDs;

    @Inject
    protected WfService wfService;

    @Inject
    protected UserSession userSession;

    @Inject
    protected WindowManagerProvider windowManagerProvider;

    protected void initFields() {
        if (cardAttachmentsFrame != null)
            attachmentsTable = getComponent("cardAttachmentsFrame.attachmentsTable");
        else
            attachmentsTable = getComponent("attachmentsTable");
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initFields();

        if (cardAttachmentsFrame == null && attachmentsTable != null) {
            //leave table init for editors which don't use cardAttachmentsFrame
            attachmentsTable.addAction(new CreateAction(attachmentsTable, WindowManager.OpenType.DIALOG) {
                @Override
                public Map<String, Object> getInitialValues() {
                    HashMap<String, Object> values = new HashMap<>();
                    values.put("card", cardDs.getItem());
                    values.put("file", new FileDescriptor());
                    return values;
                }
            });
            attachmentsTable.addAction(new EditAction(attachmentsTable, WindowManager.OpenType.DIALOG));
            attachmentsTable.addAction(new RemoveAction(attachmentsTable, false));
        }

        if (cardProcFrame != null) {
            cardProcFrame.init();
        }

        if (cardRolesFrame != null) {
            cardRolesFrame.init();
        }

        if (resolutionsFrame != null) {
            resolutionsFrame.init();
        }
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        AbstractWfAccessData accessData = getContext().getParamValue("accessData");
        if (accessData != null) {
            accessData.setItem(getItem());
        }

//        if (attachmentsTable != null) {
//            FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");
//            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
//        }

        if (cardProcFrame != null) {
            cardProcFrame.setCard((Card) getItem());
            if (accessData != null) {
                boolean disabled = (accessData.getDisabledComponents() != null)
                        && accessData.getDisabledComponents().contains(cardProcFrame.getId());
                cardProcFrame.setEnabled(cardProcFrame.isEnabled() && !disabled);
            }
        }

        if (cardRolesFrame != null) {
            cardRolesFrame.setCard(getItem());
            accessData = getContext().getParamValue("accessData");
            if (accessData != null) {
                boolean disabled = (accessData.getDisabledComponents() != null)
                        && accessData.getDisabledComponents().contains(cardRolesFrame.getId());
                cardRolesFrame.setEnabled(cardRolesFrame.isEnabled() && !disabled);
            }
        }

        if (resolutionsFrame != null) {
            resolutionsFrame.setCard(getItem());
        }

        final ActionsFrame actionsFrame = getComponent("actionsFrame");

        if (actionsFrame != null) {
            initActionsFrame(getItem(), actionsFrame);
        }
    }

    protected void initActionsFrame(Card card, final ActionsFrame actionsFrame) {
        if (PersistenceHelper.isNew(card)) {
            actionsFrame.initActions(card, isCommentVisible());
            cardDs.addListener(new DsListenerAdapter<T>() {
                @Override
                public void valueChanged(T source, String property, Object prevValue, Object value) {
                    if ("proc".equals(property)) {
                        actionsFrame.initActions(source, isCommentVisible());
                    }
                }
            });
        } else {
            actionsFrame.initActions(card, isCommentVisible());
        }
    }

    protected void deleteNotifications(Card card) {
        wfService.deleteNotifications(card, userSession.getCurrentOrSubstitutedUser());
    }

    protected abstract boolean isCommentVisible();

    public void reopen(Map<String, Object> parameters) {
        WindowManager.OpenType openType = WindowManager.OpenType.THIS_TAB;
        close("cancel", true);
        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo(this.getId());
        WindowManager webWindowManager = windowManagerProvider.get();
        webWindowManager.openEditor(windowInfo, getItem(), openType, parameters);
    }

    protected void initAttachments(Card item) {
        if (attachmentsDs != null) {
            List<CardAttachment> cas = new LinkedList<>();
            if (item.getAttachments() != null && !item.getAttachments().isEmpty()) {
                for (CardAttachment ca : item.getAttachments()) {
                    if (PersistenceHelper.isNew(ca) && !attachmentsDs.containsItem(ca.getId())) {
                        cas.add(ca);
                    }
                }
            }
            for (CardAttachment ca : cas) {
                attachmentsDs.addItem(ca);
            }
        }
    }
}
