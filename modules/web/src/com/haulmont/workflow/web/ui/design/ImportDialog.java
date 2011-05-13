/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Devyatkin
 * Created: 31.03.11 9:53
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.cuba.core.app.FileUploadService;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.FileUploadField;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.Map;

public class ImportDialog extends AbstractWindow {
    byte[] bytes;

    public ImportDialog(IFrame frame) {
        super(frame);
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);
        final FileUploadField fileUploadField = getComponent("fileUpload");
        fileUploadField.addListener(new FileUploadField.Listener() {
            public void uploadStarted(Event event) {
            }

            public void uploadFinished(Event event) {
            }

            public void uploadSucceeded(Event event) {
                FileUploadService service = ServiceLocator.lookup(FileUploadService.NAME);
                File file = service.getFile(fileUploadField.getFileId());

                InputStream fileInput = null;
                try {
                    fileInput = new FileInputStream(file);
                    bytes = IOUtils.toByteArray(fileInput);
                    fileInput.close();
                    service.deleteFile(fileUploadField.getFileId());
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
