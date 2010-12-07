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

import com.haulmont.cuba.core.app.FileStorageService;
import com.haulmont.cuba.core.app.FileUploadService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.AttachmentType;
import com.vaadin.ui.Select;

import java.util.*;
import java.util.List;

public class AttachmentsMultiUploader extends AbstractEditor {
    private List<Attachment> attachments = new ArrayList<Attachment>();

    private FileMultiUploadField uploadField = null;
    private Button okBtn, cancelBtn, delBtn;
    private boolean needSave;
    private CollectionDatasource attachDs = null;
    private Table uploadsTable = null;
    private Map<FileDescriptor, UUID> descriptors = new HashMap<FileDescriptor, UUID>();
    private AttachmentCreator creator = null;
    private LookupField attachTypeCombo = null;
    private Label labelProgress = null;

    private boolean isUploading = false;

    private CollectionDatasource attachTypesDs, filesDs = null;
    private AttachmentType defaultAttachType = null;

    public AttachmentsMultiUploader(IFrame frame) {
        super(frame);
    }

    @Override
    public void setItem(Entity item) {
        // Do nothing
        defaultAttachType = getDefaultAttachmentType();
        okBtn.setEnabled(false);

        Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
        select.select(defaultAttachType);

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
    protected void init(Map<String, Object> params) {
        super.init(params);

        creator = (AttachmentCreator) params.get("creator");

        uploadsTable = getComponent("uploadsTable");
        attachTypeCombo = getComponent("attachTypeCombo");

        attachTypesDs = getDsContext().get("attachTypesDs");
        attachTypesDs.refresh();

        filesDs = getDsContext().get("filesDs");
        
        attachDs = uploadsTable.getDatasource();
        attachDs.refresh();

        labelProgress = getComponent("fileProgress");

        okBtn = getComponent("windowActions.windowCommit");
        delBtn = getComponent("removeAttachBtn");
        cancelBtn = getComponent("windowActions.windowClose");

        TableActionsHelper helper = new TableActionsHelper(this, uploadsTable);
        helper.createRemoveAction();

        uploadField = getComponent("multiUpload");
        uploadField.setCaption(getMessage("upload"));

        Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
        select.setImmediate(true);
        select.setNullSelectionAllowed(true);
        select.setFilteringMode(Select.FILTERINGMODE_CONTAINS);
        select.setItemCaptionMode(Select.ITEM_CAPTION_MODE_EXPLICIT);

        Collection ids = attachTypesDs.getItemIds();
        Iterator iter = ids.iterator();
        while (iter.hasNext()) {
            AttachmentType type = (AttachmentType) attachTypesDs.getItem(iter.next());
            select.addItem(type);
            select.setItemCaption(type, type.getLocName());
        }
        select.select(null);

        attachTypeCombo.addListener(new ValueListener() {

            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                if ((value != null) && (value instanceof AttachmentType)) {
                    Collection ids = attachDs.getItemIds();
                    Iterator iter = ids.iterator();
                    while (iter.hasNext()) {
                        Attachment item = (Attachment) attachDs.getItem(iter.next());
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

                FileUploadService uploader = ServiceLocator.lookup(FileUploadService.NAME);
                Map<UUID, String> uploads = uploadField.getUploadsMap();
                Iterator<Map.Entry<UUID, String>> iterator = uploads.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<UUID, String> item = iterator.next();

                    FileDescriptor fDesc = uploader.getFileDescriptor(item.getKey(), item.getValue());
                    filesDs.addItem(fDesc);
                    Attachment attach = creator.createObject();
                    attach.setComment("");
                    attach.setName(fDesc.getName());
                    attach.setFile(fDesc);
                    attach.setCreateTs(TimeProvider.currentTimestamp());

                    Select select = (Select) WebComponentsHelper.unwrap(attachTypeCombo);
                    AttachmentType aType = (AttachmentType) select.getValue();

                    if (aType != null)
                        attach.setAttachType(aType);
                    else
                        attach.setAttachType(defaultAttachType);

                    descriptors.put(fDesc, item.getKey());
                    attachDs.addItem(attach);
                }
                uploads.clear();
                uploadsTable.refresh();
                labelProgress.setValue("");
            }

            @Override
            public void fileUploadStart(String fileName) {
                isUploading = true;
                okBtn.setEnabled(false);
                delBtn.setEnabled(false);
                String progressString = MessageProvider.getMessage(getClass(), "fileUploading") + ":" + fileName;
                labelProgress.setValue(progressString);
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
        ((AttachmentUploadDatasource) attachDs).setModified(false);
        if (commit()) {
            if (needSave) {
                saveFile();
            }
            close(COMMIT_ACTION_ID);
        }
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

    public static int safeLongToInt(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException
                    (l + " cannot be cast to int without changing its value.");
        }
        return (int) l;
    }

    private void saveFile() {
        FileUploadService uploader = ServiceLocator.lookup(FileUploadService.NAME);
        FileStorageService fss = ServiceLocator.lookup(FileStorageService.JNDI_NAME);
        try {
            // Relocate the file from temporary storage to permanent
            Collection ids = attachDs.getItemIds();
            Iterator iter = ids.iterator();
            while (iter.hasNext()) {
                Attachment attach = (Attachment) attachDs.getItem(iter.next());
                UUID fileId = descriptors.get(attach.getFile());
                fss.putFile(attach.getFile(), uploader.getFile(fileId));
                attachments.add(attach);
            }
        } catch (FileStorageException e) {
            throw new RuntimeException(e);
        }
    }
}