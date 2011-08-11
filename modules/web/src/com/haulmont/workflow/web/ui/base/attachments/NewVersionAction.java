/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.Collection;

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
        owner.setSelected((Entity) null);
    }

    @Override
    public String getCaption() {
        return MessageProvider.getMessage(getClass(), "actions.newVersion");
    }

}
