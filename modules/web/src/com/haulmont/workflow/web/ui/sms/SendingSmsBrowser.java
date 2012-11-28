/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.sms;

import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ListActionType;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author novikov
 */
public class SendingSmsBrowser extends AbstractLookup {

    @Inject
    protected Table table;

    public SendingSmsBrowser(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        ComponentsHelper.createActions(table, EnumSet.of(ListActionType.REFRESH));
        ComponentsHelper.createActions(table, EnumSet.of(ListActionType.REMOVE));
    }
}
