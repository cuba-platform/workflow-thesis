/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import org.apache.commons.lang.BooleanUtils;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DeployDesignWindow extends AbstractWindow {

    private Design design;
    private LookupField procField;
    private LookupField roleField;
    private CheckBox newProcField;
    private String errorMsg;

    @Inject
    protected CollectionDatasource<Proc, UUID> procDs;

    @Override
    public void init(Map<String, Object> params) {
        getDialogParams().setWidthAuto();

        design = (Design) params.get("design");
        procDs.refresh();

        Label designNameLab = (Label) getComponentNN("designNameLab");
        designNameLab.setValue(design.getName());

        procField = (LookupField) getComponent("procField");

        roleField = (LookupField) getComponent("roleField");

        newProcField = (CheckBox) getComponentNN("newProcField");
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
                            procField.setValue(findDesignProc());
                            roleField.setEnabled(false);
                        }
                    }
                }
        );

        Proc designProc = findDesignProc();
        if (designProc != null) {
            newProcField.setValue(false);
            procField.setValue(designProc);
        }

        Button deployBtn = (Button) getComponentNN("deployBtn");
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

        Button cancelBtn = (Button) getComponentNN("cancelBtn");
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

    private Proc findDesignProc() {
        for (Proc proc : procDs.getItems()) {
            if (design.equals(proc.getDesign()))
                return proc;
        }
        return null;
    }
}
