/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.11 16:39
 *
 * $Id$
 */
package workflow.client.web.ui.design;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;

import java.util.Map;

public class NotificationMatrixWindow extends AbstractEditor {

    private Datasource<Design> ds;
    private FileUploadField uploadField;
    private Button okBtn;
    private TextField fileNameField;

    public NotificationMatrixWindow(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        ds = getDsContext().get("designDs");

        okBtn = getComponent("windowActions.windowCommit");
        okBtn.setEnabled(false);

        fileNameField = getComponent("fileNameField");

        uploadField = getComponent("uploadField");
        uploadField.addListener(
                new FileUploadField.Listener() {
                    public void uploadStarted(Event event) {
                    }

                    public void uploadFinished(Event event) {
                    }

                    public void uploadSucceeded(Event event) {
                        ds.getItem().setNotificationMatrix(uploadField.getBytes());
                        ds.getItem().setNotificationMatrixUploaded(true);

                        DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
                        service.saveNotificationMatrixFile(ds.getItem());

                        fileNameField.setEditable(true);
                        fileNameField.setValue(uploadField.getFilePath());
                        fileNameField.setEditable(false);

                        okBtn.setEnabled(true);
                    }

                    public void uploadFailed(Event event) {
                    }

                    public void updateProgress(long readBytes, long contentLength) {
                    }
                }
        );
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        okBtn.setEnabled(false);
    }
}
