/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.design;

import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import org.apache.commons.lang.BooleanUtils;

import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DeployDesignWindow extends AbstractWindow {

    private Design design;
    private LookupField procField;
    private LookupField roleField;
    private String errorMsg;

    @Override
    public void init(Map<String, Object> params) {
        design = (Design) params.get("design");

        Label designNameLab = getComponent("designNameLab");
        designNameLab.setValue(design.getName());

        procField = getComponent("procField");

        roleField = getComponent("roleField");

        CheckBox newProcField = getComponent("newProcField");
        newProcField.setValue(true);
        newProcField.addListener(
                new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        if (BooleanUtils.isTrue((Boolean) value)) {
                            procField.setValue(null);
                            procField.setEnabled(false);
                            roleField.setEnabled(true);
                        } else {
                            procField.setEnabled(true);
                            roleField.setEnabled(false);
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
                            deploy(null, roleField.<Role>getValue());
                        } else {
                            showOptionDialog(
                                    getMessage("confirmDeploy.title"),
                                    String.format(getMessage("confirmDeploy.msg"), proc.getName()),
                                    MessageType.CONFIRMATION,
                                    new Action[]{
                                            new DialogAction(DialogAction.Type.YES) {
                                                @Override
                                                public void actionPerform(Component component) {
                                                    deploy(proc, null);
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

    void deploy(Proc proc, Role role) {
        DesignerService service = ServiceLocator.lookup(DesignerService.NAME);
        try {
            service.deployDesign(design.getId(), proc == null ? null : proc.getId(), role);
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
