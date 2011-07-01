/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 25.11.2010 16:21:43
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.attachments;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.cuba.web.jmx.FileUploadingAPI;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.AttachmentType;
import com.vaadin.ui.Select;

import java.util.*;
import java.util.List;

public class AttachmentsMultiUploader extends AbstractEditor {
    private static final long serialVersionUID = 5049622742528690083L;

    private List<Attachment> attachments = new ArrayList<Attachment>();

    private FileMultiUploadField uploadField;
    private Button okBtn, cancelBtn, delBtn;
    private boolean needSave;
    private Table uploadsTable = null;
    private Map<FileDescriptor, UUID> descriptors = new HashMap<FileDescriptor, UUID>();
    private AttachmentCreator creator;
    private LookupField attachTypeCombo;

    private boolean isUploading = false;

    private CollectionDatasourceImpl attachDs, attachTypesDs, filesDs;
    private AttachmentType defaultAttachType;

    public AttachmentsMultiUploader(IFrame frame) {
        super(frame);
    }

    @Override
    public void setItem(Entity item) {
        // Do nothing
        if (defaultAttachType == null) {
            defaultAttachType = getDefaultAttachmentType();
        }
        okBtn.setEnabled(false);

        Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
        select.select(defaultAttachType);

        cancelBtn.setAction(new AbstractAction("actions.Cancel") {
            private static final long serialVersionUID = 6603819180519108350L;

            // OnClose
            public void actionPerform(Component component) {
                if (AttachmentsMultiUploader.this.isUploading)
                    AttachmentsMultiUploader.this.showOptionDialog(
                            getMessage("uploadStopRequest"),
                            getMessage("uploadStopRequest"),
                            MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.YES) {
                                        private static final long serialVersionUID = -1090801189008445909L;

                                        @Override
                                        public void actionPerform(Component component) {
                                            AttachmentsMultiUploader.this.close("");
                                        }
                                    },
                                    new DialogAction(DialogAction.Type.NO)
                            });
                else
                    close("");
            }
        });
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        defaultAttachType = (AttachmentType) params.get("attachType");

        creator = (AttachmentCreator) params.get("creator");

        attachDs = getDsContext().get("attachDs");
        attachDs.valid();

        uploadsTable = getComponent("uploadsTable");
        if (uploadsTable != null) {
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(uploadsTable);
        }
        attachTypeCombo = getComponent("attachTypeCombo");

        attachTypesDs = getDsContext().get("attachTypesDs");
        attachTypesDs.refresh();

        filesDs = getDsContext().get("filesDs");

        okBtn = getComponent("windowActions.windowCommit");
        cancelBtn = getComponent("windowActions.windowClose");

        delBtn = getComponent("removeAttachBtn");
        delBtn.setAction(new AbstractAction("actions.Remove") {
            public void actionPerform(Component component) {
                FileUploadingAPI fileUploading = AppContext.getBean(FileUploadingAPI.NAME);
                for (Object item : uploadsTable.getSelected()) {
                    attachDs.excludeItem((Entity) item);

                    FileDescriptor fDesc = ((Attachment) item).getFile();
                    filesDs.removeItem(fDesc);

                    UUID fileId = descriptors.get(fDesc);
                    try {
                        fileUploading.deleteFile(fileId);
                    } catch (FileStorageException ignored) {
                    }
                    descriptors.remove(fDesc);
                }
                uploadsTable.refresh();
            }
        });

        uploadField = getComponent("multiUpload");
        uploadField.setCaption(getMessage("upload"));

        Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
        select.setImmediate(true);
        select.setNullSelectionAllowed(true);
        select.setFilteringMode(Select.FILTERINGMODE_CONTAINS);
        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);

        Collection ids = attachTypesDs.getItemIds();
        for (Object id : ids) {
            AttachmentType type = (AttachmentType) attachTypesDs.getItem(id);
            select.addItem(type);
            select.setItemCaption(type, type.getLocName());
        }
        select.select(null);

        attachTypeCombo.addListener(new ValueListener() {

            private static final long serialVersionUID = -7749607248779629771L;

            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ((value != null) && (value instanceof AttachmentType)) {
                    Collection ids = attachDs.getItemIds();
                    for (Object id : ids) {
                        Attachment item = (Attachment) attachDs.getItem(id);
                        item.setAttachType((AttachmentType) value);
                    }
                    uploadsTable.refresh();
                }
            }
        });

        uploadField.addListener(new FileMultiUploadField.UploadListener() {

            @Override
            public void queueUploadComplete() {
                needSave = true;
                isUploading = false;
                okBtn.setEnabled(true);
                delBtn.setEnabled(true);

                FileUploadingAPI fileUploading = AppContext.getBean(FileUploadingAPI.NAME);
                Map<UUID, String> uploads = uploadField.getUploadsMap();

                for (Map.Entry<UUID, String> upload : uploads.entrySet()) {
                    FileDescriptor fDesc = fileUploading.getFileDescriptor(upload.getKey(), upload.getValue());
                    filesDs.addItem(fDesc);
                    Attachment attach = creator.createObject();
                    attach.setComment("");
                    attach.setName(fDesc.getName());
                    attach.setFile(fDesc);

                    Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
                    AttachmentType aType = (AttachmentType) select.getValue();

                    if (aType != null)
                        attach.setAttachType(aType);
                    else
                        attach.setAttachType(defaultAttachType);

                    descriptors.put(fDesc, upload.getKey());
                    attachDs.includeItem(attach);
                }
                uploads.clear();
                uploadsTable.refresh();
            }

            @Override
            public void fileUploadStart(String fileName) {
                isUploading = true;
                okBtn.setEnabled(false);
                delBtn.setEnabled(false);
            }

            @Override
            public void errorNotify(String fileName, String message, int errorCode) {
                if (errorCode == FileMultiUploadField.FILE_EXCEEDS_SIZE_LIMIT) {
                    String locMessage = MessageProvider.getMessage(getClass(), "fileExceedsSizeLimit") + ":" + fileName;
                    AttachmentsMultiUploader.this.showNotification(locMessage, NotificationType.WARNING);
                } else {
                    String locMessage = MessageProvider.getMessage(getClass(), "fileUploadError") + ":" + fileName;
                    AttachmentsMultiUploader.this.showNotification(locMessage, NotificationType.ERROR);
                }
            }
        });
    }

    @Override
    public void commitAndClose() {
        attachDs.setModified(false);
        if (commit()) {
            if (needSave) {
                saveFile();
            }
            close(COMMIT_ACTION_ID);
        }
    }

    @Override
    public boolean close(String actionId) {
        uploadField.setEnabled(false);
        boolean closeResult = super.close(actionId);
        if (!closeResult)
            uploadField.setEnabled(true);

        if (closeResult && !COMMIT_ACTION_ID.equals(actionId)) {
            FileUploadingAPI fileUploading = AppContext.getBean(FileUploadingAPI.NAME);
            for (Map.Entry<FileDescriptor, UUID> upload : descriptors.entrySet()) {
                try {
                    fileUploading.deleteFile(upload.getValue());
                } catch (FileStorageException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return closeResult;
    }

    private AttachmentType getDefaultAttachmentType() {
        String defaultAttachmentCode = AppContext.getProperty("cuba.defaultAttachmentType");

        AttachmentType defaultAttachmentType = null;
        if (defaultAttachmentCode != null) {
            Collection ids = attachTypesDs.getItemIds();
            Iterator iter = ids.iterator();
            while (iter.hasNext() && (defaultAttachmentType == null)) {
                Object id = iter.next();
                AttachmentType type = (AttachmentType) attachTypesDs.getItem(id);
                if (defaultAttachmentCode.equals(type.getCode()))
                    defaultAttachmentType = type;
            }
        }

        return defaultAttachmentType;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    private void saveFile() {
        FileUploadingAPI fileUploading = AppContext.getBean(FileUploadingAPI.NAME);
        try {
            // Relocate the file from temporary storage to permanent
            Collection ids = attachDs.getItemIds();
            for (Object id : ids) {
                Attachment attach = (Attachment) attachDs.getItem(id);
                UUID fileId = descriptors.get(attach.getFile());
                fileUploading.putFileIntoStorage(fileId, attach.getFile());
                attachments.add(attach);
            }
        } catch (FileStorageException e) {
            throw new RuntimeException(e);
        }
    }
}