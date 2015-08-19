/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.AttachmentType;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author artamonov
 * @version $Id$
 */
public class AttachmentsMultiUploader extends AbstractEditor {

    private List<Attachment> attachments = new ArrayList<>();

    private FileMultiUploadField uploadField;
    private Button okBtn, cancelBtn, delBtn;
    private boolean needSave;
    private Table uploadsTable = null;
    private Map<FileDescriptor, UUID> descriptors = new HashMap<>();
    private AttachmentCreator creator;
    private LookupField attachTypeCombo;

    private boolean isUploading = false;

    private CollectionDatasourceImpl attachDs, attachTypesDs, filesDs;
    private AttachmentType defaultAttachType;

    @Override
    public void setItem(Entity item) {
        // Do nothing
        if (defaultAttachType == null) {
            defaultAttachType = getDefaultAttachmentType();
        }
        okBtn.setEnabled(false);

        attachTypeCombo.setValue(defaultAttachType);

        cancelBtn.setAction(new AbstractAction("actions.Cancel") {

            // OnClose
            public void actionPerform(Component component) {
                if (AttachmentsMultiUploader.this.isUploading)
                    AttachmentsMultiUploader.this.showOptionDialog(
                            getMessage("uploadStopRequest"),
                            getMessage("uploadStopRequest"),
                            MessageType.CONFIRMATION,
                            new Action[]{
                                    new DialogAction(DialogAction.Type.YES) {
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
    public void init(Map<String, Object> params) {
        super.init(params);

        defaultAttachType = (AttachmentType) params.get("attachType");

        creator = (AttachmentCreator) params.get("creator");

        attachDs = getDsContext().get("attachDs");
        attachDs.valid();

        uploadsTable = (Table) getComponent("uploadsTable");
        if (uploadsTable != null) {
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(uploadsTable);
        }
        attachTypeCombo = (LookupField) getComponent("attachTypeCombo");

        attachTypesDs = getDsContext().get("attachTypesDs");
        attachTypesDs.refresh();

        filesDs = getDsContext().get("filesDs");

        okBtn = (Button) getComponent("windowActions.windowCommit");
        cancelBtn = (Button) getComponent("windowActions.windowClose");

        delBtn = (Button) getComponent("removeAttachBtn");
        delBtn.setAction(new AbstractAction("actions.Remove") {
            public void actionPerform(Component component) {
                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                for (Attachment item : uploadsTable.<Attachment>getSelected()) {
                    attachDs.excludeItem(item);

                    FileDescriptor fDesc = item.getFile();
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

        uploadField = (FileMultiUploadField) getComponent("multiUpload");
        uploadField.setCaption(getMessage("upload"));

        Map<String, Object> attachmentTypesMap = new HashMap<>();
        Collection ids = attachTypesDs.getItemIds();
        for (Object id : ids) {
            AttachmentType type = (AttachmentType) attachTypesDs.getItem(id);
            attachmentTypesMap.put(type.getLocName(), type);
        }
        attachTypeCombo.setOptionsMap(attachmentTypesMap);

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

                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);

                for (Map.Entry<UUID, String> upload : uploadField.getUploadsMap().entrySet()) {
                    FileDescriptor fDesc = fileUploading.getFileDescriptor(upload.getKey(), upload.getValue());
                    filesDs.addItem(fDesc);
                    Attachment attach = creator.createObject();
                    attach.setComment("");
                    attach.setName(StringUtils.substringBeforeLast(fDesc.getName(), "."));
                    attach.setFile(fDesc);

                    AttachmentType aType = attachTypeCombo.getValue();

                    if (aType != null)
                        attach.setAttachType(aType);
                    else
                        attach.setAttachType(defaultAttachType);

                    descriptors.put(fDesc, upload.getKey());
                    attachDs.includeItem(attach);
                }
                uploadField.clearUploads();
                uploadsTable.refresh();
            }

            @Override
            public void fileUploadStart(String fileName) {
                isUploading = true;
                okBtn.setEnabled(false);
                delBtn.setEnabled(false);
            }
        });
    }

    @Override
    public void commitAndClose() {
        for (Object uuid : attachDs.getItemIds()) {
            Attachment attachment = (Attachment) attachDs.getItem(uuid);
            if (attachment.getVersionNum() == null) {
                attachment.setVersionNum(1);
            }
        }
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
            FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
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

    @Override
    protected boolean postCommit(boolean committed, boolean close) {
        return true;
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
        FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
        try {
            // Relocate the file from temporary storage to permanent
            Collection ids = attachDs.getItemIds();
            for (Object id : ids) {
                Attachment attach = (Attachment) attachDs.getItem(id);
                UUID fileId = descriptors.get(attach.getFile());
                fileUploading.putFileIntoStorage(fileId, attach.getFile());
                attach.setFile((FileDescriptor) filesDs.getItem(attach.getFile().getId()));
                attachments.add(attach);
            }
        } catch (FileStorageException e) {
            throw new RuntimeException(e);
        }
    }
}