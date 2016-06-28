/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class NotificationMatrixWindow extends AbstractEditor {

    protected FileUploadField uploadField;
    protected byte[] bytes;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        uploadField = (FileUploadField) getComponentNN("uploadField");
        uploadField.addFileUploadSucceedListener(e -> {
            try {
                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);

                InputStream inputFile = new FileInputStream(fileUploading.getFile(uploadField.getFileId()));
                bytes = IOUtils.toByteArray(inputFile);
                inputFile.close();
                fileUploading.deleteFile(uploadField.getFileId());

                close(Window.COMMIT_ACTION_ID);
            } catch (IOException | FileStorageException ex) {
                throw new RuntimeException("Load failed", ex);
            }
        });
    }

    public byte[] getBytes(){
        return bytes;
    }
}