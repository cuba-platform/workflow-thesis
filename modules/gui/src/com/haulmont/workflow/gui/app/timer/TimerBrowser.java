/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.timer;

import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.RefreshAction;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.TimerEntity;

import javax.inject.Inject;
import java.util.Map;

public class TimerBrowser extends AbstractLookup {

    @Inject
    private Table<TimerEntity> table;

    @Inject
    private  WfService service;

    public TimerBrowser() {
        super();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        table.addAction(new AbstractAction("fireTimer") {
            @Override
            public void actionPerform(Component component) {
                TimerEntity timer = table.getSingleSelected();
                if (timer == null)
                    return;
                service.processTimer(timer);
                table.getDatasource().refresh();

            }
        });
        table.addAction(new RefreshAction(table));
    }
}