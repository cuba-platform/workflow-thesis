/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author devyatkin
 * @version $Id$
 */
public class ImportDialog extends AbstractWindow {
    byte[] bytes;

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        final FileUploadField fileUploadField = getComponent("fileUpload");
        fileUploadField.addListener(new FileUploadField.Listener() {
            public void uploadStarted(Event event) {
            }

            public void uploadFinished(Event event) {
            }

            public void uploadSucceeded(Event event) {
                FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);
                File file = fileUploading.getFile(fileUploadField.getFileId());

                InputStream fileInput = null;
                try {
                    fileInput = new FileInputStream(file);
                    bytes = IOUtils.toByteArray(fileInput);
                    fileInput.close();
                    fileUploading.deleteFile(fileUploadField.getFileId());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (FileStorageException e) {
                    throw new RuntimeException(e);
                }

                close(Window.COMMIT_ACTION_ID);

            }

            public void uploadFailed(Event event) {
            }

            public void updateProgress(long readBytes, long contentLength) {
            }
        });
    }
}
