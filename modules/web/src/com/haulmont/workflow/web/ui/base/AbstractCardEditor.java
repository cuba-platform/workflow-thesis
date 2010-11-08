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
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.web.ui.base.action.ActionsFrame;
import com.vaadin.ui.*;
import com.vaadin.ui.Component;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

public abstract class AbstractCardEditor extends AbstractEditor {

    protected Datasource<Card> cardDs;
    protected CollectionDatasource<CardRole, UUID> cardRolesDs;
    protected Table attachmentsTable;

    protected CardProcFrame cardProcFrame;
    protected CardRolesFrame cardRolesFrame;
    protected ResolutionsFrame resolutionsFrame;

    public AbstractCardEditor(IFrame frame) {
        super(frame);
    }

    protected void initFields() {
        cardDs = getDsContext().get("cardDs");
        cardRolesDs = getDsContext().get("cardRolesDs");
        attachmentsTable = getComponent("attachmentsTable");
        cardProcFrame = getComponent("cardProcFrame");
        cardRolesFrame = getComponent("cardRolesFrame");
        resolutionsFrame = getComponent("resolutionsFrame");
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

        if (attachmentsTable != null) {
            FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");
        }

        if (cardProcFrame != null) {
            cardProcFrame.setCard((Card) getItem());
            AbstractAccessData accessData = getContext().getParamValue("accessData");
            if (accessData != null) {
                boolean disabled = (accessData.getDisabledComponents() != null)
                        && accessData.getDisabledComponents().contains(cardProcFrame.getId());
                cardProcFrame.setEnabled(cardProcFrame.isEnabled() && !disabled);
            }
        }

        if (cardRolesFrame != null) {
            cardRolesFrame.setCard((Card) getItem());
            AbstractAccessData accessData = getContext().getParamValue("accessData");
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
            if (card.getProc() != null) {
                actionsFrame.initActions(card, isCommentVisible());
            }
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

    protected void reopen(Map<String, Object> parameters) {
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

        WindowInfo windowInfo = AppConfig.getInstance().getWindowConfig().getWindowInfo(this.getId());
        WebWindowManager webWindowManager = App.getInstance().getWindowManager();
        webWindowManager.openEditor(windowInfo, getItem(), openType, parameters);
    }
}
