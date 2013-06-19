/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowParams;
import com.haulmont.cuba.gui.components.AbstractAccessData;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.ReassignInfo;
import com.haulmont.workflow.core.global.WfConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author krivopustov
 * @version $Id$
 */
public abstract class AbstractWfAccessData extends AbstractAccessData {
    protected WfService wfService;

    protected Entity item;
    protected AssignmentInfo info;

    public AbstractWfAccessData(Map<String, Object> params) {
        super(params);
        init(params);
    }

    protected void init(Map<String, Object> params) {
        item = WindowParams.ITEM.getEntity(params);
    }

    public void setItem(Entity item) {
        this.item = item;
        wfService = AppBeans.get(WfService.NAME);
        info = wfService.getAssignmentInfo((Card) item);
    }

    /**
     * Assignment info for current user and card
     * if accessData exists then it used in ActionsFrame
     *
     * @return AssignmentInfo
     */
    public AssignmentInfo getAssignmentInfo() {
        return info;
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

    public ReassignInfo getReassignInfo() {
        return null;
    }

    public List<String> getVisibleActions(Card card) {
        List<String> visibleActions = new ArrayList<>();
        visibleActions.add(WfConstants.ACTION_SAVE);
        visibleActions.add(WfConstants.ACTION_SAVE_AND_CLOSE);
        visibleActions.add(WfConstants.ACTION_START);
        visibleActions.add(WfConstants.ACTION_CANCEL);
        visibleActions.add(WfConstants.ACTION_REASSIGN);
        if (info != null) {
            visibleActions.addAll(info.getActions());
        }
        return visibleActions;
    }

    public List<String> getEnabledActions(Card card) {
        return getVisibleActions(card);
    }
}
