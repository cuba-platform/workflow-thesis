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
