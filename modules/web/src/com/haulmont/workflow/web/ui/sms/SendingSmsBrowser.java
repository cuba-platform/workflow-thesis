/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.sms;

import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.ListActionType;
import com.haulmont.workflow.core.entity.SendingSms;
import com.haulmont.workflow.core.enums.SmsStatus;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

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
        table.addAction(new AbstractAction("repeatSend") {
            @Override
            public void actionPerform(Component component) {
                Set selected= table.getSelected();
                for(Object obj:selected){
                    ((SendingSms)obj).setDateStartSending(TimeProvider.currentTimestamp());
                    ((SendingSms)obj).setStatus(SmsStatus.IN_QUEUE);
                    ((SendingSms)obj).setAttemptsCount(0);
                }
                table.getDatasource().commit();
            }
        });
    }
}
