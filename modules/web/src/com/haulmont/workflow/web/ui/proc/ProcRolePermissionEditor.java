/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRolePermission;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.ProcRolePermissionValue;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcRolePermissionEditor extends AbstractEditor {
    private Proc proc;
    private ProcRolePermission permission;
    private LookupField stateLookup;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        proc = (Proc)params.get("param$proc");
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        permission = (ProcRolePermission) getItem();

        LookupField value = getComponent("value");
        value.setValue(ProcRolePermissionValue.ALLOW);

        LookupField type = getComponent("type");
        type.setValue(ProcRolePermissionType.ADD);

        stateLookup = getComponent("state");
        Map<String, Object> statesMap = new HashMap<String, Object>();
        String states = proc.getStates();
        if (StringUtils.isNotBlank(states)) {
            for (String state : states.split("\\s*,\\s*")) {
                String locState = MessageProvider.getMessage(proc.getMessagesPack(), state);
                statesMap.put(locState, state);
            }
        }
        statesMap.put(AppBeans.get(Messages.class).getMainMessage(WfConstants.PROC_NOT_ACTIVE), WfConstants.PROC_NOT_ACTIVE);
        stateLookup.setOptionsMap(statesMap);
        stateLookup.setValue(permission.getState());
    }

    @Override
    public void commitAndClose() {
        permission.setState(stateLookup.<String>getValue());
        super.commitAndClose();
    }
}
