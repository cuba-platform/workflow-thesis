/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Frame;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.gui.app.tools.AttachmentActionTools;

import java.util.*;

public class NewVersionAction extends CreateAction {

    protected Attachment prevVersion;

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
        prevVersion = (Attachment) target.getSingleSelected();
        Collection<Attachment> attachments = target.getSelected();
        if (prevVersion == null || attachments.size() > 1) {
            target.getFrame().showNotification(AppBeans.get(Messages.class).getMessage(getClass(), "selectAttachmentForVersion"),
                    Frame.NotificationType.WARNING);
            return;
        }

        Map<String, Object> map = getInitialValues();
        if (map != null && map.containsKey("card")) {
            Card card = (Card) map.get("card");
            if (!PersistenceHelper.isNew(card)) {
                target.getDatasource().refresh();
                prevVersion = (Attachment) target.getDatasource().getItem(prevVersion.getId());
                if (prevVersion == null) {
                    target.getFrame().showNotification(AppBeans.get(Messages.class).getMessage(getClass(), "warning.itemWasDeleted"),
                            Frame.NotificationType.HUMANIZED);
                    target.setSelected((Entity) null);
                    return;
                }
            }
        }

        super.actionPerform(component);
        target.getFrame().getContext().getParams().remove("prevVersion");
    }

    @Override
    protected void afterCommit(Entity entity) {
        Attachment newVersion = (Attachment) entity;
        CollectionDatasource<Attachment, UUID> attachmentsDs = target.getDatasource();
        AttachmentActionTools attachmentActionTools = AppBeans.get(AttachmentActionTools.class);
        List<Attachment> prevVersions = attachmentActionTools.getPrevVersionOfAttachments(attachmentsDs.getItems(), prevVersion);
        Map<String, Object> initialValues = getInitialValues();
        Card card = (Card) initialValues.get("card");
        if (!PersistenceHelper.isNew(card)) {
            attachmentsDs.refresh();
        }
        prevVersion = attachmentActionTools.findPrevVersion(attachmentsDs, prevVersion, prevVersions);
        attachmentsDs.addItem(newVersion);
        attachmentActionTools.updateAttachmentVersions(attachmentsDs, newVersion, prevVersion);
        commitNewVersion(newVersion);
    }

    protected void commitNewVersion(Attachment version) {
        CollectionDatasource attachmentsDs = target.getDatasource();
        Map<String, Object> initialValues = getInitialValues();
        if (initialValues != null && initialValues.containsKey("card")) {
            Card card = (Card) initialValues.get("card");
            if (!PersistenceHelper.isNew(card)) {
                attachmentsDs.commit();
                attachmentsDs.refresh();
            }
        }
        target.setSelected((Entity) null);
    }

    @Override
    public String getCaption() {
        return AppBeans.get(Messages.class).getMessage(getClass(), "actions.newVersion");
    }

    @Override
    public Map<String, Object> getWindowParams() {
        if (windowParams == null) {
            windowParams = new HashMap<>();
        }
        windowParams.put("prevVersion", prevVersion);
        return windowParams;
    }
}