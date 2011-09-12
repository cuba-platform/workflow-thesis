/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 28.06.2010 10:10:38
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.entity.ProcRole;

import java.util.Map;

public class ProcRoleEditor extends AbstractEditor {


    public ProcRoleEditor(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        if(!PersistenceHelper.isNew((ProcRole)params.get("item"))){
            getComponent("code").setEnabled(false);
        }
    }
}
