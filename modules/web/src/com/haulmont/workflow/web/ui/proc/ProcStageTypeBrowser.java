/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TableActionsHelper;

import java.util.Map;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class ProcStageTypeBrowser extends AbstractWindow {

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        Table table = getComponent("table");
        TableActionsHelper helper = new TableActionsHelper(this, table);
        helper.createCreateAction(WindowManager.OpenType.DIALOG);
        helper.createEditAction(WindowManager.OpenType.DIALOG);
        helper.createRemoveAction();
    }
}