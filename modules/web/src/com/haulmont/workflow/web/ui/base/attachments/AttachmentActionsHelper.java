/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.UserSessionProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.PropertyDatasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.entity.EntityOp;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.cuba.web.filestorage.WebExportDisplay;
import com.haulmont.cuba.web.gui.components.WebButtonsPanel;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.AttachmentType;

import javax.annotation.Nullable;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Create {@link Attachment} actions and buttons for attachments table
 */
@SuppressWarnings("unused")
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
                    ArrayList<Attachment> selected = new ArrayList<Attachment>();
                    for (Attachment descriptor : descriptors) {
                        selected.add(descriptor);
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
        final AttachmentCreator propsSetter = creator;
        final CollectionDatasource attachDs = attachmentsTable.getDatasource();
        final UserSession userSession = UserSessionProvider.getUserSession();
        final List<String> exclTypes = params == null ? null: (List<String>)params.get("exclTypes");
        return new AbstractAction(PASTE_ACTION_ID) {
            @Override
            public void actionPerform(Component component) {
                List<Attachment> buffer = AttachmentCopyHelper.get();
                Set<String> bufferIncludedTypes = new HashSet<String>();
                if ((buffer != null) && (buffer.size() > 0)) {
                    if (exclTypes != null) {
                        for (Attachment attachment : buffer) {
                            if (exclTypes.contains(attachment.getAttachType().getCode())) {
                                String type = attachment.getAttachType().getLocName();
                                bufferIncludedTypes.add(type);
                            }
                        }
                        if (!bufferIncludedTypes.isEmpty()) {
                            StringBuilder info = new StringBuilder(MessageProvider.getMessage(getClass(), "messages.bufferContainsExclTypes"));
                            info.append("<ul>");
                            for (String type : bufferIncludedTypes) {
                                info.append("<li>" + type + "</li>");
                            }
                            info.append("</ul>");
                            attachments.getFrame().showNotification(info.toString(), IFrame.NotificationType.WARNING);
                            bufferIncludedTypes.clear();
                            return;
                        }
                    }
                    for (Attachment attach : buffer) {
                        Attachment attachment = propsSetter.createObject();
                        attachment.setFile(attach.getFile());
                        attachment.setComment(attach.getComment());
                        attachment.setName(attach.getName());
                        attachment.setUuid(UUID.randomUUID());
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
                                find = obj.getFile().getUuid() == fileUid;
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
                    String info = MessageProvider.getMessage(getClass(), "messages.bufferEmptyInfo");
                    attachments.getFrame().showNotification(info, IFrame.NotificationType.HUMANIZED);
                }
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && userSession.isEntityOpPermitted(attachDs.getMetaClass(), EntityOp.CREATE);
            }
        };
    }

    /**
     * Create load attachment context menu for attaghments table
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     * @return Action
     */
    public static void createLoadAction(Table attachmentsTable, IFrame window) {
        final Table attachments = attachmentsTable;
        attachments.addAction(new AbstractAction(LOAD_ACTION_ID) {

            @Override
            public void actionPerform(Component component) {
                Set<Attachment> selected = attachments.getSelected();
                if (selected.size() == 1) {
                    FileDescriptor fd = selected.iterator().next().getFile();

                    new WebExportDisplay(false).show(fd);
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
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator) {
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
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, final AttachmentCreator creator,
                                                 final WindowManager.OpenType openType, final Map<String, Object> params) {
        final Table attachments = attachmentsTable;
        final CollectionDatasource attachDs = attachmentsTable.getDatasource();
        final IFrame frame = window;
        final AttachmentCreator fCreator = creator;
        final UserSession userSession = UserSessionProvider.getUserSession();

        return new AbstractAction("actions.MultiUpload") {
            @Override
            public void actionPerform(Component component) {
                Map<String, Object> paramz = new HashMap<String, Object>();
                if (params != null) {
                    paramz.putAll(params);
                }
                paramz.put("creator", fCreator);

                final Window editor = frame.openEditor("wf$AttachUploader", null,
                        openType,
                        paramz, null);

                editor.addListener(new Window.CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        if (Window.COMMIT_ACTION_ID.equals(actionId) && editor instanceof Window.Editor) {
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
                    }
                });
            }

            @Override
            public boolean isEnabled() {
                return super.isEnabled() && userSession.isEntityOpPermitted(attachDs.getMetaClass(), EntityOp.CREATE);
            }
        };
    }

    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator,
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

        final IFrame frame = attachmentsTable.getFrame();

        WebButtonsPanel buttonsPanel = (WebButtonsPanel) attachmentsTable.getButtonsPanel();
        final FileUploadField fileUploadField = AppConfig.getFactory().createComponent(FileUploadField.NAME);
        fileUploadField.setFrame(frame);
        fileUploadField.addListener(new FileUploadField.Listener() {
            @Override
            public void uploadStarted(Event event) {
            }

            @Override
            public void uploadFinished(Event event) {
            }

            @Override
            public void uploadSucceeded(Event event) {
                Map<String, Object> openParams = new HashMap<String, Object>();
                UUID fileId = fileUploadField.getFileId();
                String filename = event.getFilename();
                openParams.put("fileId", fileId);
                if (windowParams != null)
                    openParams.putAll(windowParams);

                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                FileDescriptor fileDescriptor = fileUploading.getFileDescriptor(fileId, filename);

                Attachment attachment = creator.createObject();
                attachment.setFile(fileDescriptor);

                final Window.Editor editor = frame.openEditor(uploadScreenId, attachment, openType, openParams);
                editor.addListener(new Window.CloseListener() {
                    @Override
                    public void windowClosed(String actionId) {
                        if (Window.Editor.COMMIT_ACTION_ID.equals(actionId)) {
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
                    }
                });
            }

            @Override
            public void uploadFailed(Event event) {
            }
        });
        fileUploadField.setCaption(MessageProvider.getMessage(AttachmentActionsHelper.class, "wf.upload.submit"));
        buttonsPanel.add(fileUploadField);

        return fileUploadField;
    }
}