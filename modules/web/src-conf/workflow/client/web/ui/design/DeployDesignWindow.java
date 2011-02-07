/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.12.10 10:53
 *
 * $Id$
 */
package workflow.client.web.ui.design;

import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

public class DeployDesignWindow extends AbstractWindow {

    private Design design;
    private LookupField procField;

    private String errorMsg;

    public DeployDesignWindow(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        design = (Design) params.get("design");

        Label designNameLab = getComponent("designNameLab");
        designNameLab.setValue(design.getName());

        procField = getComponent("procField");

        CheckBox newProcField = getComponent("newProcField");
        newProcField.setValue(true);
        newProcField.addListener(
                new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (BooleanUtils.isTrue((Boolean) value)) {
                            procField.setValue(null);
                            procField.setEnabled(false);
                        } else {
                            procField.setEnabled(true);
                        }
                    }
                }
        );

        Button deployBtn = getComponent("deployBtn");
        deployBtn.setAction(
                new AbstractAction("deployBtn") {
                    public void actionPerform(Component component) {
                        final Proc proc = procField.getValue();
                        if (proc == null) {
                            deploy(null);
                        } else {
                            showOptionDialog(
                                    getMessage("confirmDeploy.title"),
                                    String.format(getMessage("confirmDeploy.msg"), proc.getName()),
                                    MessageType.CONFIRMATION,
                                    new Action[]{
                                            new DialogAction(DialogAction.Type.YES) {
                                                @Override
                                                public void actionPerform(Component component) {
                                                    deploy(proc);
                                                }
                                            },
                                            new DialogAction(DialogAction.Type.NO)
                                    }
                            );

                        }
                    }
                }
        );

        Button cancelBtn = getComponent("cancelBtn");
        cancelBtn.setAction(
                new AbstractAction("cancelBtn") {
                    public void actionPerform(Component component) {
                        close("cancel");
                    }
                }
        );
    }

    void deploy(Proc proc) {
        DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
        try {
            service.deployDesign(design.getId(), proc == null ? null : proc.getId());
            close("ok");
        } catch (DesignDeploymentException e) {
            errorMsg = e.getMessage();
            close("error");
        }
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}
