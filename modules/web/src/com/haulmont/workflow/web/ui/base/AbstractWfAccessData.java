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

import com.haulmont.cuba.core.entity.Entity;
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

    private Entity item;

    public AbstractWfAccessData(Map<String, Object> params) {
        super(params);
    }

    public void setItem(Entity item) {
        this.item = item;
    }

    public abstract boolean getSaveEnabled();

    public abstract boolean getSaveAndCloseEnabled();

    /**
     * Affects Start Process action inside ActionsFrame
     */
    public abstract boolean getStartProcessEnabled();

    /**
     * Affects Start Process action inside CardProcFrame
     */
    public boolean getStartCardProcessEnabled() {
        return true;
    }

    /**
     * Affects Adding Process action inside CardProcFrame
     */
    public boolean getAddCardProcessEnabled() {
        return true;
    }

    /**
     * Affects Remove Process action inside CardProcFrame
     */
    public boolean getRemoveCardProcessEnabled() {
        return true;
    }

    public boolean getCancelProcessEnabled() {
        return false;
    }

    public List<String> getVisibleActions(Card card) {
        List<String> visibleActions = new ArrayList<String>();
        visibleActions.add(WfConstants.ACTION_SAVE);
        visibleActions.add(WfConstants.ACTION_SAVE_AND_CLOSE);
        visibleActions.add(WfConstants.ACTION_START);
        visibleActions.add(WfConstants.ACTION_CANCEL);
        WfService wfs = ServiceLocator.lookup(WfService.NAME);
        AssignmentInfo info = wfs.getAssignmentInfo(card);
        if (info != null) {
            visibleActions.addAll(info.getActions());
        }
        return visibleActions;
    };

    public List<String> getEnabledActions(Card card) {
        return getVisibleActions(card);
    }
}
