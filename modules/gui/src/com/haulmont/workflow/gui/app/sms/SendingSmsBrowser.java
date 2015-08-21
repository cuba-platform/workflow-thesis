/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.sms;

import com.haulmont.cuba.core.global.TimeSource;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.AbstractLookup;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.Table;
import com.haulmont.cuba.gui.components.actions.ListActionType;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * @author novikov
 * @version $Id$
 */
public class SendingSmsBrowser extends AbstractLookup {

    @Inject
    protected Table table;

    @Inject
    protected TimeSource timeSource;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        ComponentsHelper.createActions(table, EnumSet.of(ListActionType.REFRESH));
        ComponentsHelper.createActions(table, EnumSet.of(ListActionType.REMOVE));
        table.addAction(new AbstractAction("repeatSend") {
            @Override
            public void actionPerform(Component component) {
                Set selected= table.getSelected();
                for(Object obj:selected){
                    ((SendingSms)obj).setStartSendingDate(timeSource.currentTimestamp());
                    ((SendingSms)obj).setStatus(SmsStatus.IN_QUEUE);
                    ((SendingSms)obj).setAttemptsCount(0);
                    ((SendingSms)obj).setErrorCode(0);
                }
                table.getDatasource().commit();
            }

            @Override
            public String getCaption() {
                return getMessage("SendingSms.repeatSendMsg");
            }
        });
    }
}