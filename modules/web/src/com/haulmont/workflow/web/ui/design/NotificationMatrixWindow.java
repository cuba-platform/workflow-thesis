/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.11 16:39
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.web.jmx.FileUploadingAPI;
import org.apache.commons.io.IOUtils;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class NotificationMatrixWindow extends AbstractEditor {

    private FileUploadField uploadField;
    private byte[] bytes;

    public NotificationMatrixWindow(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);
        uploadField = getComponent("uploadField");
        uploadField.addListener(
                new FileUploadField.Listener() {
                    public void uploadStarted(Event event) {
                    }

                    public void uploadFinished(Event event) {
                    }

                    public void uploadSucceeded(Event event) {
                        //bytes = uploadField.getBytes();
                        try {
                            FileUploadingAPI fileUploading = AppContext.getBean(FileUploadingAPI.NAME);

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
