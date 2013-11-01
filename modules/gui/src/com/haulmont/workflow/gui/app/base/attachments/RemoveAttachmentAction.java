/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.base.attachments;


import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
public class RemoveAttachmentAction extends RemoveAction {
    private static final long serialVersionUID = -5912958394607863731L;
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
        CollectionDatasource datasource = owner.getDatasource();
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
                owner.getFrame().getDialogParams().setWidth(500);
                Window window = owner.getFrame().openWindow("wf$RemoveAttachmentConfirmDialog", WindowManager.OpenType.DIALOG);

                window.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        if (actionId.equals(RemoveAttachmentConfirmDialog.OPTION_LAST_VERSION)) {
                            migrateToNewLastVersion(selected);
                            doRemove(selected, autocommit);
                        } else if (actionId.equals(RemoveAttachmentConfirmDialog.OPTION_ALL_VERSIONS)) {
                            doRemove(getAllVersions(selected), autocommit);
                        }
                    }
                });
            } else {
                final String messagesPackage = AppConfig.getMessagesPack();
                owner.getFrame().showOptionDialog(
                        getConfirmationTitle(messagesPackage),
                        getConfirmationMessage(messagesPackage),
                        IFrame.MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(DialogAction.Type.OK) {

                                    public void actionPerform(Component component) {
                                        migrateToNewLastVersion(selected);
                                        doRemove(selected, autocommit);
                                    }
                                }, new DialogAction(DialogAction.Type.CANCEL) {

                            public void actionPerform(Component component) {
                            }
                        }
                        }
                );
            }
        }
    }

    protected Set<Attachment> getAllVersions(Set<Attachment> selected) {
        Set allVersions = new HashSet();
        allVersions.addAll(selected);

        CollectionDatasource datasource = owner.getDatasource();
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
        User user = UserSessionClient.getUserSession().getCurrentOrSubstitutedUser();
        if (UserSessionProvider.getUserSession().getRoles().contains("Administrators"))
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
        Map<Attachment, List<Attachment>> map = new HashMap<Attachment, List<Attachment>>();
        CollectionDatasource datasource = owner.getDatasource();
        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && oldLastVesrions.contains(versionOf)) {
                java.util.List<Attachment> versions = map.get(versionOf);
                if (versions == null) {
                    versions = new ArrayList<Attachment>();
                    map.put(versionOf, versions);
                }
                versions.add(attachment);
            }
        }
        return map;
    }

    protected void migrateToNewLastVersion(Set<Attachment> oldLastVesrions) {
        Map<Attachment, List<Attachment>> map = new HashMap<Attachment, List<Attachment>>();
        CollectionDatasource datasource = owner.getDatasource();
        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && oldLastVesrions.contains(versionOf)) {
                java.util.List<Attachment> versions = map.get(versionOf);
                if (versions == null) {
                    versions = new ArrayList<Attachment>();
                    map.put(versionOf, versions);
                }
                versions.add(attachment);
            }
        }

        for (java.util.List<Attachment> list : map.values()) {
            Collections.sort(list, new Comparator<Attachment>() {
                public int compare(Attachment o1, Attachment o2) {
                    return o2.getVersionNum().compareTo(o1.getVersionNum());
                }
            });

            list.removeAll(oldLastVesrions);
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

