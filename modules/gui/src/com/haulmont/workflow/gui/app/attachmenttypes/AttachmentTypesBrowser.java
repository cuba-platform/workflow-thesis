/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.attachmenttypes;

import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.AbstractWindow;
import com.haulmont.cuba.gui.components.Table;

import javax.inject.Inject;
import java.util.Map;

public class AttachmentTypesBrowser extends AbstractWindow{

    @Inject
    protected Table table;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        ComponentsHelper.createActions(table);
    }
}
