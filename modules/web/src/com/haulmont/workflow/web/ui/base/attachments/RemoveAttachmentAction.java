/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base.attachments;


import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.ListComponent;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.web.App;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.*;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */
public class RemoveAttachmentAction extends RemoveAction {
    private static final long serialVersionUID = -5912958394607863731L;

    public RemoveAttachmentAction(ListComponent owner) {
        super(owner);
    }

    public RemoveAttachmentAction(ListComponent owner, boolean autocommit) {
        super(owner, autocommit);
    }

    public RemoveAttachmentAction(ListComponent owner, boolean autocommit, String id) {
        super(owner, autocommit, id);
    }

    @Override
    protected void confirmAndRemove(final Set selected) {
        boolean versionExists = false;
        for (Object attachment : selected) {
            for (Object id : datasource.getItemIds()) {
                Attachment versionAttachment = (Attachment) datasource.getItem(id);
                if (attachment.equals(versionAttachment.getVersionOf())) {
                    versionExists = true;
                }
            }
        }

        if (!versionExists) {
            super.confirmAndRemove(selected);
        } else {
            App.getInstance().getWindowManager().getDialogParams().setWidth(500);
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
        }
    }

    private Set<Attachment> getAllVersions(Set<Attachment> selected) {
        Set allVersions = new HashSet();
        allVersions.addAll(selected);

        for (Object id : datasource.getItemIds()) {
            Attachment attachment = (Attachment) datasource.getItem(id);
            Attachment versionOf = attachment.getVersionOf();
            if (versionOf != null && selected.contains(versionOf)) {
                allVersions.add(attachment);
            }
        }

        return allVersions;
    }

    private void migrateToNewLastVersion(Set<Attachment> oldLastVesrions) {
        Map<Attachment, List<Attachment>> map = new HashMap<Attachment, List<Attachment>>();
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
                    return o1.getVersionNum().compareTo(o2.getVersionNum());
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

}

