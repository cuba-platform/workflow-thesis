/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.IOUtils;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class ImportDialog extends AbstractWindow {
    protected byte[] bytes;

    @Inject
    protected FileUploadField fileUpload;

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        fileUpload.addListener(new FileUploadField.ListenerAdapter() {
            @Override
            public void uploadSucceeded(Event event) {
                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                File file = fileUploading.getFile(fileUpload.getFileId());

                InputStream fileInput;
                try {
                    fileInput = new FileInputStream(file);
                    bytes = IOUtils.toByteArray(fileInput);
                    fileInput.close();
                    fileUploading.deleteFile(fileUpload.getFileId());
                } catch (IOException | FileStorageException e) {
                    throw new RuntimeException("Unable to import file", e);
                }

                close(Window.COMMIT_ACTION_ID);
            }
        });
    }
}