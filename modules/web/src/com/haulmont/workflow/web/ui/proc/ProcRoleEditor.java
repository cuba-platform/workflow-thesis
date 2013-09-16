/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.AbstractEditor;

import java.util.Map;

public class ProcRoleEditor extends AbstractEditor {

    @Override
    public void init(Map<String, Object> params) {
        if (!PersistenceHelper.isNew(WindowParams.ITEM.getEntity(params))) {
            getComponent("code").setEnabled(false);
        }
    }
}
