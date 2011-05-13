/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 19.01.11 16:17
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.proc;

import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.TableActionsHelper;

import java.util.Map;

public class ProcStageTypeBrowser extends AbstractWindow {
    public ProcStageTypeBrowser(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        Table table = getComponent("table");
        TableActionsHelper helper = new TableActionsHelper(this, table);
        helper.createCreateAction(WindowManager.OpenType.DIALOG);
        helper.createEditAction(WindowManager.OpenType.DIALOG);
        helper.createRemoveAction();
    }
}
