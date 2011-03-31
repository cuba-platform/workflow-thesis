/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 22.10.2010 17:40:34
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.filestorage.FileDisplay;
import com.haulmont.workflow.core.entity.Attachment;

import java.util.*;
import java.util.List;

// Create copy/paste buttons actions for attachments table
public class AttachmentActionsHelper {
    private AttachmentActionsHelper() {
    }

    /* Create copy attachment action for table
     * @param attachmentsTable Table with attachments
     * @return Action
     */
    public static Action createCopyAction(Table attachmentsTable) {
        final Table attachments = attachmentsTable;
        return new AbstractAction("actions.Copy") {
            public void actionPerform(Component component) {
                Set descriptors = attachments.getSelected();
                if (descriptors.size() > 0) {
                    ArrayList<Attachment> selected = new ArrayList<Attachment>();
                    Iterator iter = descriptors.iterator();
                    while (iter.hasNext()) {
                        selected.add((Attachment) iter.next());
                    }
                    AttachmentCopyHelper.put(selected);
                    String info;
                    if (descriptors.size() == 1)
                        info = MessageProvider.getMessage(getClass(), "messages.copyInfo");
                    else
                        info = MessageProvider.getMessage(getClass(), "messages.manyCopyInfo");
                    attachments.getFrame().showNotification(info, IFrame.NotificationType.HUMANIZED);
                }
            }
        };
    }

    /* Create paste attachment action for table
     * @param attachmentsTable Table with attachments
     * @param creator Custom method for set object properties
     * @return Action
     */
    public static Action createPasteAction(Table attachmentsTable, AttachmentCreator creator) {
        final Table attachments = attachmentsTable;
        final AttachmentCreator propsSetter = creator;
        return new AbstractAction("actions.Paste") {
            public void actionPerform(Component component) {
                List<Attachment> buffer = AttachmentCopyHelper.get();
                if ((buffer != null) && (buffer.size() > 0)) {
                    for (Attachment attach : buffer) {
                        Attachment attachment = propsSetter.createObject();
                        attachment.setFile(attach.getFile());
                        attachment.setComment(attach.getComment());
                        attachment.setName(attach.getName());
                        attachment.setUuid(UUID.randomUUID());
                        attachment.setAttachType(attach.getAttachType());

                        FileDescriptor fd = attach.getFile();
                        if (fd != null) {
                            UUID fileUid = fd.getUuid();
                            Object[] ids = attachments.getDatasource().getItemIds().toArray();
                            boolean find = false;
                            int i = 0;
                            while ((i < ids.length) && !find) {
                                Attachment obj = (Attachment) attachments.getDatasource().getItem(ids[i]);
                                find = obj.getFile().getUuid() == fileUid;
                                i++;
                            }
                            if (!find) {
                                attachments.getDatasource().addItem(attachment);
                                attachments.refresh();
                            }
                        }
                    }
                } else {
                    String info = MessageProvider.getMessage(getClass(), "messages.bufferEmptyInfo");
                    attachments.getFrame().showNotification(info, IFrame.NotificationType.HUMANIZED);
                }
            }
        };
    }

    /* Create load attachment context menu for attaghments table
     * @param attachments Table with attachments
     * @param window Window
     * @return Action
     */
    public static void createLoadAction(Table attachmentsTable, IFrame window) {
        final Table attachments = attachmentsTable;
        attachments.addAction(new AbstractAction("actions.Load") {

            public void actionPerform(Component component) {
                Set selected = attachments.getSelected();
                if (selected.size() == 1) {
                    FileDescriptor fd = ((Attachment) selected.iterator().next()).getFile();

                    FileDisplay fileDisplay = new FileDisplay(true);
                    fileDisplay.show(fd.getName(), fd, true);
                }
            }
        });
    }

    /**
     * Create action for multiupload attachments
     * @param attachmentsTable Table with attachments
     * @param window Window
     * @param creator Custom method for set object properties
     */
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator){
        return  createMultiUploadAction(attachmentsTable,window,creator,WindowManager.OpenType.THIS_TAB);
    }

    /**
     * Create action for multiupload attachments
     * @param attachmentsTable Table with attachments
     * @param window Window
     * @param creator Custom method for set object properties
     * @param openType Window open type
     */
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator,
                                                 final WindowManager.OpenType openType, final Map<String, Object> params){
        final Table attachments = attachmentsTable;
        final CollectionDatasource attachDs = attachmentsTable.getDatasource();
        final IFrame frame = window;
        final AttachmentCreator fCreator = creator;
        final UserSession userSession = UserSessionClient.getUserSession();

        return new AbstractAction("actions.MultiUpload") {
            public void actionPerform(Component component) {
                Map<String, Object> paramz = new HashMap<String,Object>();
                if (params != null) {
                    paramz.putAll(params);
                }
                paramz.put("creator",fCreator);

                final Window editor = frame.openEditor("wf$AttachUploader", null,
                        openType,
                        paramz, null);

                editor.addListener(new Window.CloseListener() {
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId) && editor instanceof Window.Editor) {
                            List<Attachment> items = ((AttachmentsMultiUploader)editor).getAttachments();
                            for (Attachment attach : items) {
                                attachDs.addItem(attach);
                            }
                            attachments.refresh();
                        }
                    }
                });
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && userSession.isEntityOpPermitted(attachDs.getMetaClass(), EntityOp.CREATE );
            }
        };
    }

    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator,
                                                 final WindowManager.OpenType openType) {
        return createMultiUploadAction(attachmentsTable,window,creator,openType, null);
    }
}
