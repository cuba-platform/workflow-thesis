/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.10.2009 15:51:19
 *
 * $Id$
 */
package workflow.client.web.ui.attachment

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
import com.haulmont.cuba.gui.data.ValueListener
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.gui.data.DsContext
import com.haulmont.workflow.core.entity.AttachmentType
import com.haulmont.cuba.core.global.MessageProvider
import org.apache.openjpa.kernel.DelegatingResultList
import com.haulmont.cuba.gui.data.CollectionDatasource
import com.haulmont.cuba.core.sys.AppContext
import com.haulmont.cuba.gui.UserSessionClient

public class AttachmentEditor extends AbstractEditor {

  private Datasource<Attachment> attachmentDs
  private Datasource<FileDescriptor> fileDs
  private Button okBtn
  private TextField nameText
  private TextField fileNameText
  private Label extLabel
  private Label sizeLab
  private Label createDateLab
  private FileUploadField uploadField
  private LookupField attachType
  private AttachmentType defaultAType
  private CollectionDatasource attachTypesDs

  private boolean needSave

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
      defaultAType = getDefaultAttachmentType()
      attachType.setValue(defaultAType)
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

                extLabel.setValue(FileDownloadHelper.getFileExt(uploadField.getFileName()))
                sizeLab.setValue(uploadField.getBytes().length)
                createDateLab.setValue(TimeProvider.currentTimestamp())

                okBtn.setEnabled(true)

                needSave = true
              }
      ] as Listener)
    } else {
      uploadField.setEnabled(false)
      fileNameText.setEditable(false)
    }
  }

  private AttachmentType getDefaultAttachmentType() {
    String defaultAttachmentCode = AppContext.getProperty('cuba.defaultAttachmentType')
    AttachmentType defaultAttachmentType
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

  private void saveFile() {
    FileStorageService fss = ServiceLocator.lookup(FileStorageService.JNDI_NAME)
    try {
      fss.saveFile(fileDs.getItem(), uploadField.getBytes())
    } catch (FileStorageException e) {
      throw new RuntimeException(e)
    }
  }
}
