/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 28.05.2010 16:08:00
 *
 * $Id$
 */
package workflow.client.web.ui.proc;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRolePermission;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.ProcRolePermissionValue;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class ProcRolePermissionEditor extends AbstractEditor {
    private Proc proc;
    private ProcRolePermission permission;
    private LookupField stateLookup;

    public ProcRolePermissionEditor(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
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
        statesMap.put(MessageProvider.getMessage(AppContext.getProperty(AppConfig.MESSAGES_PACK_PROP), WfConstants.PROC_NOT_ACTIVE), WfConstants.PROC_NOT_ACTIVE);
        stateLookup.setOptionsMap(statesMap);
        stateLookup.setValue(permission.getState());
    }

    @Override
    public void commitAndClose() {
        permission.setState(stateLookup.<String>getValue());
        super.commitAndClose();
    }
}
