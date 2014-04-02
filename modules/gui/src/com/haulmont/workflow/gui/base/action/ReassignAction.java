/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.base.action;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ComponentsHelper;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.AbstractAction;
import com.haulmont.cuba.gui.components.Component;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.ReassignInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.app.base.ActionsFrame;
import com.haulmont.workflow.gui.base.AbstractWfAccessData;

import java.util.Collections;

/**
 * @author subbotin
 * @version $Id$
 */
public class ReassignAction extends AbstractAction {
    protected Messages messages = AppBeans.get(Messages.NAME);

    private Card card;
    private ActionsFrame frame;

    public ReassignAction(Card card, ActionsFrame actionsFrame) {
        super(WfConstants.ACTION_REASSIGN);
        this.card = card;
        this.frame = actionsFrame;
    }

    @Override
    public void actionPerform(Component component) {
        final Window window = ComponentsHelper.getWindow(frame);
        if (commitCardEditor(window)) {
            AbstractWfAccessData accessData = window.getContext().getParamValue("accessData");
            ReassignInfo reassignInfo = accessData.getReassignInfo();
            if (reassignInfo == null) {
                window.showNotification(messages.getMessage(ReassignAction.class, "actionHasAlreadyDone"), IFrame.NotificationType.ERROR);
                return;
            }
            Window w = window.openWindow("reassign.form", WindowManager.OpenType.DIALOG, ImmutableMap.<String, Object>builder()
                    .put("card", reloadCard(reassignInfo.getCard() != null ? reassignInfo.getCard() : card))
                    .put("state", WfUtils.trimState(card))
                    .put("role", reassignInfo.getRole())
                    .put("visibleRoles", Joiner.on(",").join(reassignInfo.getVisibleRoles() == null ? Collections.emptyList() : reassignInfo.getVisibleRoles()))
                    .put("commentVisible", reassignInfo.isCommentVisible())
                    .put("commentRequired", reassignInfo.isCommentRequired())
                    .build());
            w.addListener(new Window.CloseListener() {
                @Override
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        onCommit();
                        window.close(Window.COMMIT_ACTION_ID);
                    }
                }
            });
        }
    }

    public String getCaption() {
        if (card.getProc() != null)
            return messages.getMessage(card.getProc().getMessagesPack(), getId());
        else
            return messages.getMessage(AppConfig.getMessagesPack(), getId());
    }

    protected boolean commitCardEditor(Window window) {
        return (window instanceof Window.Editor) && ((Window.Editor) window).commit();
    }

    protected void onCommit() {
    }

    protected Card reloadCard(Card card) {
        if (card.getProc() == null)
            return frame.getDsContext().getDataSupplier().reload(card, "edit");
        return card;
    }
}
