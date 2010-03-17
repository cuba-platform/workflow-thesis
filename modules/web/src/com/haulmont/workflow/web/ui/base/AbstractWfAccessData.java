/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 07.12.2009 13:25:20
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.AbstractAccessData;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractWfAccessData extends AbstractAccessData {

    public AbstractWfAccessData(Map<String, Object> params) {
        super(params);
    }

    public abstract boolean getSaveEnabled();

    public abstract boolean getSaveAndCloseEnabled();

    public abstract boolean getStartProcessEnabled();

    public List<String> getVisibleActions(Card card) {
        List<String> visibleActions = new ArrayList<String>();
        visibleActions.add(WfConstants.ACTION_SAVE);
        visibleActions.add(WfConstants.ACTION_SAVE_AND_CLOSE);
        visibleActions.add(WfConstants.ACTION_START);
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        AssignmentInfo info = wfs.getAssignmentInfo(card);
        if (info != null) {
            visibleActions.addAll(info.getActions());
        }
        return visibleActions;
    };
}
