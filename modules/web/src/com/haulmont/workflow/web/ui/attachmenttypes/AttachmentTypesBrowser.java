/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Yuryi Artamonov
 * Created: 03.11.2010 17:01:15
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.attachmenttypes;

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
