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

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.cuba.gui.ServiceLocator
import com.haulmont.cuba.core.global.PersistenceHelper
import com.haulmont.cuba.core.global.TimeProvider
import com.haulmont.cuba.core.global.FileStorageException
import com.haulmont.cuba.core.app.FileStorageService
import com.haulmont.cuba.core.entity.FileDescriptor
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.cuba.web.app.FileDownloadHelper
import com.haulmont.cuba.gui.components.FileUploadField.Listener.Event
import com.haulmont.cuba.gui.components.FileUploadField.Listener
import org.apache.commons.lang.StringUtils
import com.haulmont.workflow.core.entity.Attachment

import com.haulmont.workflow.core.entity.AttachmentType
import com.haulmont.cuba.core.global.MessageProvider

import com.haulmont.cuba.gui.data.CollectionDatasource

import com.haulmont.cuba.gui.UserSessionClient
import java.text.NumberFormat
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper
import com.haulmont.workflow.core.entity.CardAttachment
import java.text.DecimalFormat
import com.haulmont.cuba.core.app.FileUploadService
import com.haulmont.cuba.core.global.ConfigProvider
import com.haulmont.workflow.core.global.WfConfig

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

  protected boolean needSave

  public AttachmentEditor(IFrame frame) {
    super(frame)
  }

  @Override
  protected void init(Map<String, Object> params) {
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
  }

  @Override
  public void setItem(Entity item) {
    super.setItem(item)

    boolean isNew = PersistenceHelper.isNew(fileDs.getItem())

    if (isNew) {
      if (attachTypesDs.getState() !=
              com.haulmont.cuba.gui.data.Datasource.State.VALID) {
        attachTypesDs.refresh()
      }
      if (((Attachment)item).getAttachType()==null) {
        defaultAType = getDefaultAttachmentType()
        attachType.setValue(defaultAType)
      }
      attachmentDs.getItem().setFile(new FileDescriptor())
      attachmentDs.getItem().setCreateTs(TimeProvider.currentTimestamp());
      attachmentDs.getItem().setCreatedBy(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser().getLoginLowerCase());
      fileDs.refresh()

      okBtn.setEnabled(false)

      uploadField.addListener([
              updateProgress: {long readBytes, long contentLength -> },
              uploadFailed: {Event event -> },
              uploadFinished: {Event event -> },
              uploadStarted: {Event event -> },

              uploadSucceeded: {Event event ->
                String fileName = uploadField.getFileName()

                fileNameText.setValue(fileName)
                nameText.setValue(fileName[0..fileName.lastIndexOf('.') - 1])

                if (StringUtils.isBlank(nameText.getValue().toString()))
                  nameText.setValue(uploadField.getFileName())

                DecimalFormat formatter = new DecimalFormat("###,###,###,###");

                FileUploadService uploadService = ServiceLocator.lookup(FileUploadService.NAME);
                File tmpFile = uploadService.getFile(uploadField.getFileId());

                extLabel.setValue(FileDownloadHelper.getFileExt(uploadField.getFileName()))
                sizeLab.setValue(formatSize(tmpFile.length(), 0) + " (" + formatter.format(tmpFile.length()) +
                        " " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB") + ")")
                FileDescriptor fileDescriptor = getDsContext().get("fileDs").getItem()
                if (fileDescriptor)
                  fileDescriptor.size = tmpFile.length()
                createDateLab.setValue(TimeProvider.currentTimestamp())

                okBtn.setEnabled(true)

                needSave = true
              }
      ] as Listener)
    } else {
      if (item != null && item instanceof CardAttachment) {
        CardAttachment cardAttachment = (CardAttachment) item
        FileDescriptor file = cardAttachment.file
        if (file) {
          if (file.size != null ) {
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
      attachTypesDs.getItemIds().each{UUID itemId ->
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
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtMb"));
        }
        val = size / 1024;
        if (val > 10) {
            return fmt.format(val).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtKb"));
        }
        return fmt.format(size).concat(" " + MessageProvider.getMessage(AttachmentColumnGeneratorHelper.class, "fmtB"));
    }

  protected void saveFile() {
    FileStorageService fss = ServiceLocator.lookup(FileStorageService.NAME)
    FileUploadService uploadService = ServiceLocator.lookup(FileUploadService.NAME);
    try {
        UUID fileId = uploadField.getFileId();
        File file = uploadService.getFile(fileId);
        fss.putFile(fileDs.getItem(), file);
        uploadService.deleteFile(fileId);
    } catch (FileStorageException e) {
      throw new RuntimeException(e)
    }
  }
}
