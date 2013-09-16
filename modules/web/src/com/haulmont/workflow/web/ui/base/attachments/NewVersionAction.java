/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;

import java.util.Collection;
import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
public class NewVersionAction extends CreateAction {
    private Attachment prevVersion;

    public NewVersionAction(ListComponent owner) {
        super(owner);
    }

    public NewVersionAction(ListComponent owner, WindowManager.OpenType openType) {
        super(owner, openType);
    }

    public NewVersionAction(ListComponent owner, WindowManager.OpenType openType, String id) {
        super(owner, openType, id);
    }

    @Override
    public void actionPerform(Component component) {
        prevVersion = owner.getSingleSelected();
        Collection<Attachment> attachments = owner.getSelected();
        if (prevVersion == null || attachments.size() > 1) {
            App.getInstance().getWindowManager().showNotification(MessageProvider.getMessage(getClass(), "selectAttachmentForVersion"), IFrame.NotificationType.WARNING);
            return;
        }

        Map<String, Object> map = getInitialValues();
        if (map != null && map.containsKey("card")) {
            Card card = (Card) map.get("card");
            if (!PersistenceHelper.isNew(card)) {
                owner.getDatasource().refresh();
                prevVersion = (Attachment) owner.getDatasource().getItem(prevVersion.getId());
                if (prevVersion == null) {
                    App.getInstance().getWindowManager().showNotification(MessageProvider.getMessage(getClass(), "warning.itemWasDeleted"), IFrame.NotificationType.HUMANIZED);
                    owner.setSelected((Entity) null);
                    return;
                }
            }
        }

        prevVersion = prevVersion.getVersionOf() == null ? prevVersion : prevVersion.getVersionOf();

        super.actionPerform(component);
    }

    @Override
    protected void afterCommit(Entity entity) {
        Attachment newVersion = (Attachment) entity;

        prevVersion.setVersionOf(newVersion);
        if (prevVersion.getVersionNum() == null) {
            prevVersion.setVersionNum(1);
        }
        newVersion.setVersionNum(prevVersion.getVersionNum() + 1);

        CollectionDatasource attachmentsDs = owner.getDatasource();
        for (Object id : attachmentsDs.getItemIds()) {
            Attachment item = (Attachment) attachmentsDs.getItem(id);
            if (prevVersion.equals(item.getVersionOf())) {
                item.setVersionOf(newVersion);
            }
        }

        attachmentsDs.updateItem(newVersion);
        Map<String, Object> initialValues = getInitialValues();
        if (initialValues != null && initialValues.containsKey("card")) {
            Card card = (Card) initialValues.get("card");
            if (!PersistenceHelper.isNew(card)) {
                attachmentsDs.commit();
                attachmentsDs.refresh();
            }
        }
        owner.setSelected((Entity) null);
    }

    @Override
    public String getCaption() {
        return MessageProvider.getMessage(getClass(), "actions.newVersion");
    }

}
