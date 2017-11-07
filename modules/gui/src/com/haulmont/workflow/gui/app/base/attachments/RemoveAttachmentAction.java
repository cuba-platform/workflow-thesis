/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.WindowManager.OpenType;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.DialogAction.Type;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

public class RemoveAttachmentAction extends RemoveAction {

    protected AttachmentCreator.CardGetter cardGetter;

    public RemoveAttachmentAction(ListComponent owner) {
        super(owner);
    }

    public RemoveAttachmentAction(ListComponent owner, boolean autocommit) {
        super(owner, autocommit);
    }

    public RemoveAttachmentAction(ListComponent owner, boolean autocommit, String id) {
        super(owner, autocommit, id);
    }

    public RemoveAttachmentAction(ListComponent owner, AttachmentCreator.CardGetter cardGetter, String id) {
        super(owner, false, id);
        this.cardGetter = cardGetter;
    }

    @Override
    protected void confirmAndRemove(final Set selected) {
        boolean versionExists = false;
        CollectionDatasource datasource = target.getDatasource();
        for (Object attachment : selected) {
            for (Object id : datasource.getItemIds()) {
                Attachment versionAttachment = (Attachment) datasource.getItem(id);
                if (attachment.equals(versionAttachment.getVersionOf())) {
                    versionExists = true;
                }
            }
        }

        if (this.cardGetter != null)
            this.autocommit = !PersistenceHelper.isNew(this.cardGetter.getCard());
        if (!versionExists) {
            super.confirmAndRemove(selected);
        } else {
            if (userIsCreatorAllAttachments(selected)) {
                Window window = target.getFrame().openWindow("wf$RemoveAttachmentConfirmDialog",
                        OpenType.DIALOG.width(500));

                window.addCloseListener(actionId -> {
                    if (actionId.equals(RemoveAttachmentConfirmDialog.OPTION_LAST_VERSION)) {
                        migrateToNewLastVersion(selected);
                        doRemove(selected, autocommit);
                    } else if (actionId.equals(RemoveAttachmentConfirmDialog.OPTION_ALL_VERSIONS)) {
                        doRemove(getAllVersions(selected), autocommit);
                    }

                    // move focus to owner
                    target.requestFocus();
                });
            } else {
                target.getFrame().showOptionDialog(
                        getConfirmationTitle(),
                        getConfirmationMessage(),
                        Frame.MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(Type.OK) {

                                    @Override
                                    public void actionPerform(Component component) {
                                        migrateToNewLastVersion(selected);
                                        doRemove(selected, autocommit);

                                        // move focus to owner
                                        target.requestFocus();
                                    }
                                },
                                new DialogAction(Type.CANCEL, Status.PRIMARY) {
                                    @Override
                                    public void actionPerform(Component component) {
                                        // move focus to owner
                                        target.requestFocus();
                                    }
                                }
                        }
                );
            }
        }
    }

    protected Set<Attachment> getAllVersions(Set<Attachment> selected) {
        Set<Attachment> allVersions = new HashSet<>();
        allVersions.addAll(selected);

        CollectionDatasource datasource = target.getDatasource();
        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && selected.contains(versionOf)) {
                allVersions.add(attachment);
            }
        }

        return allVersions;
    }

    protected Boolean userIsCreatorAllAttachments(Set<Attachment> oldLastVesrions) {
        User user = AppBeans.get(UserSessionSource.class).getUserSession().getCurrentOrSubstitutedUser();
        // PROBLEM constant role name usage
        if (AppBeans.get(UserSessionSource.class).getUserSession().getRoles().contains("Administrators"))
            return true;
        Map<Attachment, List<Attachment>> map = getMapVersions(oldLastVesrions);
        for (java.util.List<Attachment> list : map.values()) {
            for (Attachment attachment : list) {
                if (!user.getLogin().equals(attachment.getCreatedBy()))
                    return false;
            }
        }
        return true;
    }

    protected Map<Attachment, List<Attachment>> getMapVersions(Set<Attachment> oldLastVesrions) {
        Map<Attachment, List<Attachment>> map = new HashMap<>();
        CollectionDatasource datasource = target.getDatasource();
        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && oldLastVesrions.contains(versionOf)) {
                java.util.List<Attachment> versions = map.get(versionOf);
                if (versions == null) {
                    versions = new ArrayList<>();
                    map.put(versionOf, versions);
                }
                versions.add(attachment);
            }
        }
        return map;
    }

    protected void migrateToNewLastVersion(Set<Attachment> oldLastVersions) {
        Map<Attachment, List<Attachment>> map = new HashMap<>();
        CollectionDatasource datasource = target.getDatasource();
        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && oldLastVersions.contains(versionOf)) {
                java.util.List<Attachment> versions = map.get(versionOf);
                if (versions == null) {
                    versions = new ArrayList<>();
                    map.put(versionOf, versions);
                }
                versions.add(attachment);
            }
        }

        for (java.util.List<Attachment> list : map.values()) {
            Collections.sort(list, new Comparator<Attachment>() {
                @Override
                public int compare(Attachment o1, Attachment o2) {
                    return o2.getVersionNum().compareTo(o1.getVersionNum());
                }
            });

            list.removeAll(oldLastVersions);
            if (!list.isEmpty()) {
                Attachment newVersion = list.get(0);
                newVersion.setVersionOf(null);
                datasource.updateItem(newVersion);
                list.remove(newVersion);
                for (Attachment attachment : list) {
                    attachment.setVersionOf(newVersion);
                    datasource.updateItem(newVersion);
                }
            }
        }
    }

    @Override
    protected void afterRemove(Set selected) {
        super.afterRemove(selected);
        if (this.cardGetter != null) {
            Card card = cardGetter.getCard();
            if (CollectionUtils.isNotEmpty(card.getAttachments()))
                card.getAttachments().removeAll(selected);
        }
    }
}