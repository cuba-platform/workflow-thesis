/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.10.2009 15:51:19
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.attachment

import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.gui.UserSessionClient
import com.haulmont.cuba.gui.components.FileUploadField.Listener
import com.haulmont.cuba.gui.components.FileUploadField.Listener.Event
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.upload.FileUploadingAPI
import com.haulmont.cuba.web.app.FileDownloadHelper
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.workflow.core.entity.Attachment
import com.haulmont.workflow.core.entity.AttachmentType
import com.haulmont.workflow.core.entity.CardAttachment
import com.haulmont.workflow.core.global.WfConfig
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper
import java.text.DecimalFormat
import java.text.NumberFormat
import org.apache.commons.lang.StringUtils
import com.haulmont.cuba.core.global.*
import com.haulmont.cuba.gui.components.*

public class AttachmentEditor extends AbstractEditor {

    protected Datasource<Attachment> attachmentDs
    protected Datasource<FileDescriptor> fileDs
    protected Button okBtn
    protected TextField nameText
    protected TextField fileNameText
    protected Label extLabel
    protected Label sizeLab
    protected Label createDateLab
    protected FileUploadField uploadField
    protected LookupField attachType
    protected AttachmentType defaultAType
    protected CollectionDatasource attachTypesDs
    protected Assignment assignmnet;

    protected boolean needSave
    protected UUID fileId

    public AttachmentEditor(IFrame frame) {
        super(frame)
    }

    @Override public void init(Map<String, Object> params) {
        super.init(params)

        attachmentDs = getDsContext().get("attachmentDs")
        fileDs = getDsContext().get("fileDs")

        okBtn = getComponent("windowActions.windowCommit")

        uploadField = getComponent("frame.uploadField")
        fileNameText = getComponent("frame.fileName")
        nameText = getComponent("frame.name")
        extLabel = getComponent("frame.extension")
        sizeLab = getComponent("frame.size")
        createDateLab = getComponent("frame.createDate")
        attachType = getComponent("frame.attachType")
        attachTypesDs = attachType.getOptionsDatasource()
        assignmnet = params.get('assignmnet')

        fileId = params.get('fileId')
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item)

        boolean isNew = PersistenceHelper.isNew(fileDs.getItem())
        if (assignmnet != null && item instanceof CardAttachment)
            ((CardAttachment) item).setAssignment(assignmnet);

        if (isNew) {
            if (attachTypesDs.getState() !=
                    com.haulmont.cuba.gui.data.Datasource.State.VALID) {
                attachTypesDs.refresh()
            }
            if (((Attachment) item).getAttachType() == null) {
                defaultAType = getDefaultAttachmentType()
                attachType.setValue(defaultAType)
            }

            def fdItem = fileDs.getItem()
            if ((fdItem != null) && (fdItem.createDate != null)) {
                uploadField.setVisible(false)
                needSave = true
                String fileName = fdItem.name
                nameText.setValue(fileName[0..fileName.lastIndexOf('.') - 1])
            } else {
                def attachItem = attachmentDs.getItem()
                attachItem.setFile(new FileDescriptor())
                attachItem.setCreateTs(TimeProvider.currentTimestamp());
                attachItem.setCreatedBy(UserSessionClient.getUserSession()
                        .getCurrentOrSubstitutedUser().getLoginLowerCase());

                fileDs.refresh()
                okBtn.setEnabled(false)
                uploadField.addListener([
                        updateProgress: {long readBytes, long contentLength -> },
                        uploadFailed: {Event event -> },
                        uploadFinished: {Event event -> },
                        uploadStarted: {Event event -> },

                        uploadSucceeded: {Event event ->
                            String fileName = uploadField.getFileName()
                            fileId = uploadField.getFileId()

                            fileNameText.setValue(fileName)
                            nameText.setValue(fileName[0..fileName.lastIndexOf('.') - 1])

                            if (StringUtils.isBlank(nameText.getValue().toString()))
                                nameText.setValue(uploadField.getFileName())

                            DecimalFormat formatter = new DecimalFormat("###,###,###,###");

                            FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                            File tmpFile = fileUploading.getFile(fileId);

                            extLabel.setValue(FileDownloadHelper.getFileExt(fileName))
                            sizeLab.setValue(formatSize(tmpFile.length(), 0)
                                    + " ("
                                    + formatter.format(tmpFile.length())
                                    + " "
                                    + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB") + ")")
                            FileDescriptor fileDescriptor = getDsContext().get("fileDs").getItem()
                            if (fileDescriptor)
                                fileDescriptor.size = tmpFile.length()
                            createDateLab.setValue(TimeProvider.currentTimestamp())

                            okBtn.setEnabled(true)

                            needSave = true
                        }
                ] as Listener)
            }
        } else {
            if (item != null && item instanceof CardAttachment) {
                CardAttachment cardAttachment = (CardAttachment) item
                FileDescriptor file = cardAttachment.file
                if (file) {
                    if (file.size != null) {
                        sizeLab.setValue(formatSize(file.size, 0) + " (" + file.size + ")")
                    }
                }
            }
            uploadField.setEnabled(false)
            fileNameText.setEditable(false)
        }
    }

    protected AttachmentType getDefaultAttachmentType() {
        String defaultAttachmentCode = ConfigProvider.getConfig(WfConfig.class).getDefaultAttachmentType()
        AttachmentType defaultAttachmentType = null
        if (defaultAttachmentCode != null) {
            attachTypesDs.getItemIds().each {UUID itemId ->
                AttachmentType attachmentType = attachTypesDs.getItem(itemId)
                if (attachmentType.code == defaultAttachmentCode) {
                    defaultAttachmentType = attachmentType
                    return
                }
            }
        }

        return defaultAttachmentType
    }

    def void commitAndClose() {
        if (needSave) {
            saveFile()
        }
        Attachment attachment = attachmentDs.getItem();
        if (attachment != null && attachment.getVersionNum() == null)
            attachment.setVersionNum(1);
        if (attachment.card && PersistenceHelper.isNew(attachment.card))
            super.close(COMMIT_ACTION_ID, true)
        else
            super.commitAndClose();
    }

    protected String formatSize(long longSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
            fmt.setMaximumFractionDigits(decimalPos);
        }
        final double size = longSize;
        double val = size / (1024 * 1024);
        if (val > 1) {
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class,
                    "fmtMb"));
        }
        val = size / 1024;
        if (val > 10) {
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class,
                    "fmtKb"));
        }
        return fmt.format(size).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class,
                "fmtB"));
    }

    protected void saveFile() {
        FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
        try {
            fileUploading.putFileIntoStorage(fileId, fileDs.getItem());
        } catch (FileStorageException e) {
            throw new RuntimeException(e)
        }
    }
}
