/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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

/**
 * @author krivopustov
 * @version $Id$
 */
public class NotificationMatrixWindow extends AbstractEditor {

    private FileUploadField uploadField;
    private byte[] bytes;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        uploadField = (FileUploadField) getComponent("uploadField");
        uploadField.addListener(
                new FileUploadField.Listener() {
                    public void uploadStarted(Event event) {
                    }

                    public void uploadFinished(Event event) {
                    }

                    public void uploadSucceeded(Event event) {
                        //bytes = uploadField.getBytes();
                        try {
                            FileUploadingAPI fileUploading = AppBeans.get(FileUploadingAPI.NAME);

                            InputStream inputFile = new FileInputStream(fileUploading.getFile(uploadField.getFileId()));
                            bytes = IOUtils.toByteArray(inputFile);
                            inputFile.close();
                            fileUploading.deleteFile(uploadField.getFileId());
                            close(Window.COMMIT_ACTION_ID);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        } catch (FileStorageException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    public void uploadFailed(Event event) {
                    }

                    public void updateProgress(long readBytes, long contentLength) {
                    }
                }
        );
    }

    public byte[] getBytes(){
        return bytes;
    }
}
