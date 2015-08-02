/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.attachment;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.AttachmentType;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.core.global.WfConfig;
import com.haulmont.workflow.gui.app.base.attachments.AttachmentColumnGeneratorHelper;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class AttachmentEditor extends AbstractEditor<Attachment> {

    @Inject
    protected Datasource<Attachment> attachmentDs;

    @Inject
    protected Datasource<FileDescriptor> fileDs;

    @Inject
    protected UserSession userSession;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Messages messages;

    @Inject
    protected Configuration configuration;

    @Inject
    protected FileUploadingAPI fileUploading;

    @Named("windowActions.windowCommit")
    protected Button okBtn;

    @Named("frame.name")
    protected TextField nameText;

    @Named("frame.fileName")
    protected TextField fileNameText;

    @Named("frame.uploadField")
    protected FileUploadField uploadField;

    @Named("frame.size")
    protected Label sizeLab;

    @Named("frame.attachType")
    protected LookupField attachType;

    protected CollectionDatasource<AttachmentType, UUID> attachTypesDs;

    protected Assignment assignment;
    protected UUID fileId;
    protected boolean isEdit = false;
    protected boolean needSave;

    protected Attachment prevVersion;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        attachTypesDs = attachType.getOptionsDatasource();
        assignment = (Assignment) params.get("assignmnet");
        fileId = (UUID) params.get("fileId");

        prevVersion = (Attachment) params.get("prevVersion");
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        boolean isNew = PersistenceHelper.isNew(fileDs.getItem());
        if (assignment != null && item instanceof CardAttachment)
            ((CardAttachment) item).setAssignment(assignment);

        if (isNew) {
            if (attachTypesDs.getState() != Datasource.State.VALID) {
                attachTypesDs.refresh();
            }
            if (((Attachment) item).getAttachType() == null) {
                if (prevVersion != null) {
                    attachType.setValue(prevVersion.getAttachType());
                } else {
                    attachType.setValue(getDefaultAttachmentType());
                }
            } else
                isEdit = true;

            FileDescriptor fdItem = fileDs.getItem();
            if ((fdItem != null) && (fdItem.getCreateDate() != null)) {
                String uploadFieldFileName = fdItem.getName();
                nameText.setValue(StringUtils.substringBeforeLast(uploadFieldFileName, "."));
                uploadField.setVisible(false);
                if (isEdit)
                    needSave = false;
                else {
                    needSave = true;
                    fileDs.setItem(fdItem);
                }
            } else {
                Attachment attachItem = attachmentDs.getItem();
                attachItem.setFile(new FileDescriptor());
                attachItem.setCreateTs(timeSource.currentTimestamp());
                attachItem.setCreatedBy(userSession.getCurrentOrSubstitutedUser().getLogin());
                attachItem.setSubstitutedCreator(userSession.getCurrentOrSubstitutedUser());
                fileDs.refresh();
                okBtn.setEnabled(false);
                uploadField.addListener(new FileUploadField.ListenerAdapter() {

                    @Override
                    public void uploadSucceeded(Event event) {
                        String uploadFieldFileName = uploadField.getFileName();
                        fileId = uploadField.getFileId();

                        fileNameText.setValue(uploadFieldFileName);
                        nameText.setValue(StringUtils.substringBeforeLast(uploadFieldFileName, "."));

                        if (StringUtils.isBlank(nameText.getValue().toString()))
                            nameText.setValue(uploadField.getFileName());

                        DecimalFormat formatter = new DecimalFormat("###,###,###,###");
                        FileDescriptor fileDescriptor = fileDs.getItem();

                        FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                        File tmpFile = fileUploading.getFile(fileId);

                        fileDescriptor.setExtension(FilenameUtils.getExtension(uploadFieldFileName));
                        if (tmpFile != null) {
                            sizeLab.setValue(formatSize(tmpFile.length(), 0)
                                    + " ("
                                    + formatter.format(tmpFile.length())
                                    + " "
                                    + messages.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB") + ")");
                        }

                        if (tmpFile != null) {
                            fileDescriptor.setSize(tmpFile.length());
                        }
                        fileDescriptor.setCreateDate(timeSource.currentTimestamp());

                        okBtn.setEnabled(true);

                        needSave = true;
                    }
                });
            }
        } else {
            uploadField.setEnabled(false);
        }
        if (item != null) {
            Attachment attachment = (Attachment) item;
            FileDescriptor file = attachment.getFile();
            if (file != null) {
                if (file.getSize() != null) {
                    sizeLab.setValue(formatSize(file.getSize(), 0) + " (" + file.getSize() + ")");
                }
            }
        }
    }

    protected AttachmentType getDefaultAttachmentType() {
        String defaultAttachmentCode = configuration.getConfig(WfConfig.class).getDefaultAttachmentType();
        AttachmentType defaultAttachmentType = null;
        if (!StringUtils.isEmpty(defaultAttachmentCode)) {
            for (AttachmentType attachmentType : attachTypesDs.getItems()) {
                if (defaultAttachmentCode.equals(attachmentType.getCode())) {
                    return attachmentType;
                }
            }
        }
        return defaultAttachmentType;
    }

    @Override
    public void commitAndClose() {
        if (needSave)
            saveFile();
        Attachment attachment = attachmentDs.getItem();
        if (attachment != null && attachment.getVersionNum() == null)
            attachment.setVersionNum(1);
        internalCommitAndClose(attachment);
    }

    protected void internalCommitAndClose(Attachment attachment) {
        if (attachment instanceof CardAttachment && PersistenceHelper.isNew(attachment) && ((CardAttachment) attachment).getCard() != null
                && attachmentDs.getCommitMode().equals(Datasource.CommitMode.DATASTORE)) {
            if (needSave) {
                Set<Entity> committedEntities = getDsContext().getDataSupplier().commit(new CommitContext(
                        Collections.singletonList(fileDs.getItem())));
                getItem().setFile((FileDescriptor) committedEntities.iterator().next());
            }
            super.close(COMMIT_ACTION_ID, true);
        } else
            super.commitAndClose();
    }

    protected String formatSize(long longSize, int decimalPos) {
        NumberFormat fmt = NumberFormat.getNumberInstance();
        if (decimalPos >= 0) {
            fmt.setMaximumFractionDigits(decimalPos);
        }

        double val = longSize / (1024 * 1024);

        if (val > 1) {
            return fmt.format(val).concat(" " + messages.getMessage(AttachmentColumnGeneratorHelper.class, "fmtMb"));
        }

        val = longSize / 1024;

        if (val > 10) {
            return fmt.format(val).concat(" " + messages.getMessage(AttachmentColumnGeneratorHelper.class, "fmtKb"));
        }

        return fmt.format(longSize).concat(" " + messages.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB"));
    }

    protected void saveFile() {
        if (!validateAll()) {
            throw new SilentException();
        }

        try {
            fileUploading.putFileIntoStorage(fileId, fileDs.getItem());
        } catch (FileStorageException e) {
            throw new RuntimeException(e);
        }
    }
}
