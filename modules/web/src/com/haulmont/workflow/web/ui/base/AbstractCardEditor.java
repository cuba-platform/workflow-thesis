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
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.web.ui.base.action.ActionsFrame;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;

import javax.inject.Inject;
import java.util.*;

public abstract class AbstractCardEditor extends AbstractEditor {

    protected Datasource<Card> cardDs;
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected Table attachmentsTable;

    protected CardProcFrame cardProcFrame;
    protected CardRolesFrame cardRolesFrame;
    protected ResolutionsFrame resolutionsFrame;
    protected CardAttachmentsFrame cardAttachmentsFrame;

    @Inject
    protected CollectionDatasource<CardAttachment, UUID> attachmentsDs;

    public AbstractCardEditor(IFrame frame) {
        super(frame);
    }

    protected void initFields() {
        cardDs = getDsContext().get("cardDs");
        cardRolesDs = getDsContext().get("cardRolesDs");
        cardProcFrame = getComponent("cardProcFrame");
        cardRolesFrame = getComponent("cardRolesFrame");
        resolutionsFrame = getComponent("resolutionsFrame");
        cardAttachmentsFrame = getComponent("cardAttachmentsFrame");
        if (cardAttachmentsFrame != null)
            attachmentsTable = getComponent("cardAttachmentsFrame.attachmentsTable");
        else
            attachmentsTable = getComponent("attachmentsTable");


    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        initFields();

        if (cardAttachmentsFrame != null) {
            cardAttachmentsFrame.init();
        } else {
            //leave table init for editors which don't use cardAttachmentsFrame
            if (attachmentsTable != null) {
                attachmentsTable.addAction(new CreateAction(attachmentsTable, WindowManager.OpenType.DIALOG) {
                    @Override
                    public Map<String, Object> getInitialValues() {
                        HashMap<String, Object> values = new HashMap<String, Object>();
                        values.put("card", cardDs.getItem());
                        values.put("file", new FileDescriptor());
                        return values;
                    }
                });
                attachmentsTable.addAction(new EditAction(attachmentsTable, WindowManager.OpenType.DIALOG));
                attachmentsTable.addAction(new RemoveAction(attachmentsTable, false));
            }
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
            cardRolesFrame.setCard((Card) getItem());
            accessData = getContext().getParamValue("accessData");
            if (accessData != null) {
                boolean disabled = (accessData.getDisabledComponents() != null)
                        && accessData.getDisabledComponents().contains(cardRolesFrame.getId());
                cardRolesFrame.setEnabled(cardRolesFrame.isEnabled() && !disabled);
            }
        }

        if (resolutionsFrame != null) {
            resolutionsFrame.setCard((Card) getItem());
        }

        final ActionsFrame actionsFrame = getComponent("actionsFrame");

        if (actionsFrame != null) {
            initActionsFrame((Card) getItem(), actionsFrame);
        }
    }

    protected void initActionsFrame(Card card, final ActionsFrame actionsFrame) {
        if (PersistenceHelper.isNew(card)) {
            actionsFrame.initActions(card, isCommentVisible());
            cardDs.addListener(new DsListenerAdapter<Card>() {
                @Override
                public void valueChanged(Card source, String property, Object prevValue, Object value) {
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
        WfService service = ServiceLocator.lookup(WfService.NAME);
        service.deleteNotifications(card, UserSessionClient.getUserSession().getCurrentOrSubstitutedUser());
    }

    protected abstract boolean isCommentVisible();

    public void reopen(Map<String, Object> parameters) {
        WindowManager.OpenType openType = WindowManager.OpenType.NEW_TAB;
        switch (App.getInstance().getAppWindow().getMode()) {
            case SINGLE:
                close("cancel", true);
                Collection<Window> windows = App.getInstance().getWindowManager().getOpenWindows();
                if (windows.size() > 0)
                    openType = WindowManager.OpenType.THIS_TAB;
                break;

            case TABBED:
                TabSheet beforeCloseMainTabsheet = App.getInstance().getAppWindow().getTabSheet();
                Component beforeCloseTab = null;
                if(beforeCloseMainTabsheet != null)
                    beforeCloseTab = beforeCloseMainTabsheet.getSelectedTab();

                close("cancel", true);

                TabSheet afterCloseMainTabsheet = App.getInstance().getAppWindow().getTabSheet();
                Component afterCloseTab = null;
                if(afterCloseMainTabsheet != null)
                    afterCloseTab = afterCloseMainTabsheet.getSelectedTab();

                if (afterCloseTab != null && afterCloseTab == beforeCloseTab)
                        openType = WindowManager.OpenType.THIS_TAB;
                break;
        }

        WindowInfo windowInfo = AppBeans.get(WindowConfig.class).getWindowInfo(this.getId());
        WebWindowManager webWindowManager = App.getInstance().getWindowManager();
        webWindowManager.openEditor(windowInfo, getItem(), openType, parameters);
    }

    protected void initAttachments(Card item) {
        if (attachmentsDs != null) {
            List<CardAttachment> cas = new LinkedList<CardAttachment>();
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
