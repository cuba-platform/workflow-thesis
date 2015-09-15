/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.PropertyDatasource;
import com.haulmont.cuba.gui.export.ExportDisplay;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.workflow.core.entity.Attachment;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Create {@link Attachment} actions and buttons for attachments table
 */
public class AttachmentActionsHelper {

    public static final String COPY_ACTION_ID = "actions.Copy";
    public static final String PASTE_ACTION_ID = "actions.Paste";
    public static final String LOAD_ACTION_ID = "actions.Load";

    private AttachmentActionsHelper() {
    }

    /**
     * Create copy attachment action for table
     *
     * @param attachmentsTable Table with attachments
     * @return Action
     */
    public static Action createCopyAction(Table attachmentsTable) {
        final Table attachments = attachmentsTable;
        return new AbstractAction(COPY_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                Set<Attachment> descriptors = attachments.getSelected();
                if (descriptors.size() > 0) {
                    ArrayList<Attachment> selected = new ArrayList<>();
                    for (Attachment descriptor : descriptors) {
                        selected.add(descriptor);
                    }
                    AttachmentCopyHelper.put(selected);
                    String info;
                    if (descriptors.size() == 1)
                        info = AppBeans.get(Messages.class).getMessage(getClass(), "messages.copyInfo");
                    else
                        info = AppBeans.get(Messages.class).getMessage(getClass(), "messages.manyCopyInfo");
                    attachments.getFrame().showNotification(info, Frame.NotificationType.HUMANIZED);
                }
            }
        };
    }

    /**
     * Create paste attachment action for table
     *
     * @param attachmentsTable Table with attachments
     * @param creator          Custom method for set object properties
     * @return Action
     */
    public static Action createPasteAction(Table attachmentsTable, final AttachmentCreator creator) {
        return createPasteAction(attachmentsTable,creator,null);
    }

    public static Action createPasteAction(Table attachmentsTable, final AttachmentCreator creator, @Nullable Map<String,Object> params) {
        final Table attachments = attachmentsTable;
        final CollectionDatasource attachDs = attachmentsTable.getDatasource();
        final List<String> exclTypes = params == null ? null: (List<String>)params.get("exclTypes");
        return new AbstractAction(PASTE_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                List<Attachment> buffer = AttachmentCopyHelper.get();
                Set<String> bufferIncludedTypes = new HashSet<>();
                if ((buffer != null) && (buffer.size() > 0)) {
                    if (exclTypes != null) {
                        for (Attachment attachment : buffer) {
                            if (exclTypes.contains(attachment.getAttachType().getCode())) {
                                String type = attachment.getAttachType().getLocName();
                                bufferIncludedTypes.add(type);
                            }
                        }
                        if (!bufferIncludedTypes.isEmpty()) {
                            StringBuilder info = new StringBuilder(AppBeans.get(Messages.class).getMessage(getClass(), "messages.bufferContainsExclTypes"));
                            info.append("<ul>");
                            for (String type : bufferIncludedTypes) {
                                info.append("<li>" + type + "</li>");
                            }
                            info.append("</ul>");
                            attachments.getFrame().showNotification(info.toString(), Frame.NotificationType.WARNING_HTML);
                            bufferIncludedTypes.clear();
                            return;
                        }
                    }
                    for (Attachment attach : buffer) {
                        Attachment attachment = creator.createObject();
                        attachment.setFile(attach.getFile());
                        attachment.setComment(attach.getComment());
                        attachment.setName(attach.getName());
                        attachment.setId(UUID.randomUUID());
                        attachment.setAttachType(attach.getAttachType());

                        FileDescriptor fd = attach.getFile();
                        if (fd != null) {
                            CollectionDatasource attachDs = attachments.getDatasource();

                            UUID fileUid = fd.getUuid();
                            Object[] ids = attachDs.getItemIds().toArray();
                            boolean find = false;
                            int i = 0;
                            while ((i < ids.length) && !find) {
                                //noinspection unchecked
                                Attachment obj = (Attachment) attachDs.getItem(ids[i]);
                                find = obj.getFile().getUuid().equals(fileUid);
                                i++;
                            }
                            if (!find) {
                                //noinspection unchecked
                                attachDs.addItem(attachment);
                                if (creator instanceof AttachmentCreator.CardAttachmentCreator &&
                                        !PersistenceHelper.isNew(
                                                ((AttachmentCreator.CardAttachmentCreator) creator).getCard())) {
                                    if (!(attachDs instanceof PropertyDatasource)) {
                                        attachDs.commit();
                                    }
                                    attachments.refresh();
                                }
                            }
                        }
                    }
                } else {
                    String info = AppBeans.get(Messages.class).getMessage(getClass(), "messages.bufferEmptyInfo");
                    attachments.getFrame().showNotification(info, Frame.NotificationType.HUMANIZED);
                }
            }

            @Override
            public boolean isEnabled() {
                Security security = AppBeans.get(Security.NAME);

                return super.isEnabled() && security.isEntityOpPermitted(attachDs.getMetaClass(), EntityOp.CREATE);
            }
        };
    }

    /**
     * Create load attachment context menu for attaghments table
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     */
    public static void createLoadAction(Table attachmentsTable, Frame window) {
        final Table attachments = attachmentsTable;
        attachments.addAction(new AbstractAction(LOAD_ACTION_ID) {

            @Override
            public void actionPerform(Component component) {
                Set<Attachment> selected = attachments.getSelected();
                if (selected.size() == 1) {
                    FileDescriptor fd = selected.iterator().next().getFile();
                    AppBeans.get(ExportDisplay.class).show(fd);
                }
            }
        });
    }

    /**
     * Create action for multiupload attachments
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     * @param creator          Custom method for set object properties
     * @return Multifile upload action
     */
    public static Action createMultiUploadAction(Table attachmentsTable, Frame window, AttachmentCreator creator) {
        return createMultiUploadAction(attachmentsTable, window, creator, WindowManager.OpenType.THIS_TAB);
    }

    /**
     * Create action for multiupload attachments
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     * @param creator          Custom method for set object properties
     * @param openType         Window open type
     * @param params           Dialog params
     * @return Multifile upload action
     */
    public static Action createMultiUploadAction(Table attachmentsTable, Frame window, final AttachmentCreator creator,
                                                 final WindowManager.OpenType openType, final Map<String, Object> params) {
        final Table attachments = attachmentsTable;
        final CollectionDatasource attachDs = attachmentsTable.getDatasource();
        final Frame frame = window;

        return new AbstractAction("actions.MultiUpload") {
            @Override
            public void actionPerform(Component component) {
                Map<String, Object> paramz = new HashMap<>();
                if (params != null) {
                    paramz.putAll(params);
                }
                paramz.put("creator", creator);

                Window.Editor editor = frame.openEditor("wf$AttachUploader", null,
                        openType,
                        paramz, null);

                editor.addCloseListener(actionId -> {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        List<Attachment> items = ((AttachmentsMultiUploader) editor).getAttachments();
                        for (Attachment attach : items) {
                            attachDs.addItem(attach);
                        }
                        if (creator instanceof AttachmentCreator.CardAttachmentCreator &&
                                !PersistenceHelper.isNew(
                                        ((AttachmentCreator.CardAttachmentCreator) creator).getCard())) {
                            if (!(attachDs instanceof PropertyDatasource)) {
                                attachDs.commit();
                            }
                            attachments.refresh();
                        }
                    }
                });
            }

            @Override
            public boolean isEnabled() {
                Security security = AppBeans.get(Security.NAME);

                return super.isEnabled() && security.isEntityOpPermitted(attachDs.getMetaClass(), EntityOp.CREATE);
            }
        };
    }

    public static Action createMultiUploadAction(Table attachmentsTable, Frame window, AttachmentCreator creator,
                                                 final WindowManager.OpenType openType) {
        return createMultiUploadAction(attachmentsTable, window, creator, openType, null);
    }

    /**
     * Create single click upload button in {@link ButtonsPanel}
     *
     * @param attachmentsTable table
     * @param creator          Attachment creator
     * @param uploadScreenId   Attachment editor screen
     * @param windowParams     Additional params
     * @param openType         Screen open type
     * @return FileUploadField button
     */
    public static FileUploadField createFastUploadButton(final Table attachmentsTable,
                                                         final AttachmentCreator creator,
                                                         final String uploadScreenId,
                                                         @Nullable final Map<String, Object> windowParams,
                                                         final WindowManager.OpenType openType) {

        checkNotNull(attachmentsTable.getButtonsPanel());
        checkNotNull(attachmentsTable.getFrame());
        checkNotNull(creator);

        final Frame frame = attachmentsTable.getFrame();
        final FileUploadField fileUploadField = AppConfig.getFactory().createComponent(FileUploadField.class);
        fileUploadField.setFrame(frame);

        fileUploadField.addFileUploadSucceedListener(e -> {
            Map<String, Object> openParams = new HashMap<>();
            UUID fileId = fileUploadField.getFileId();
            String filename = e.getFileName();
            openParams.put("fileId", fileId);
            if (windowParams != null)
                openParams.putAll(windowParams);
            if (openParams.containsKey("prevVersion")) {
                openParams.remove("prevVersion");
            }

            FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
            FileDescriptor fileDescriptor = fileUploading.getFileDescriptor(fileId, filename);

            Attachment attachment = creator.createObject();
            attachment.setFile(fileDescriptor);

            Window.Editor editor = frame.openEditor(uploadScreenId, attachment, openType, openParams);
            editor.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    CollectionDatasource attachDs = attachmentsTable.getDatasource();
                    attachDs.addItem(editor.getItem());
                    if (creator instanceof AttachmentCreator.CardAttachmentCreator &&
                            !PersistenceHelper.isNew(
                                    ((AttachmentCreator.CardAttachmentCreator) creator).getCard())) {
                        CollectionDatasource datasource = attachmentsTable.getDatasource();
                        if (!(datasource instanceof PropertyDatasource)) {
                            datasource.commit();
                        }
                        attachmentsTable.refresh();
                    }
                }
            });
        });
        fileUploadField.setCaption(AppBeans.get(Messages.class).getMessage(AttachmentActionsHelper.class, "wf.upload.submit"));
        return fileUploadField;
    }
}