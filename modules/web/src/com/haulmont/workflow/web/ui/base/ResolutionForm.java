/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.01.2010 16:57:01
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ResolutionForm extends AbstractForm {

    private TextField commentText;
    private Table attachmentsTable;

    public ResolutionForm(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        commentText = getComponent("commentText");
        attachmentsTable = getComponent("attachmentsTable");

        Card card = (Card) params.get("param$card");
        String messagesPack = card.getProc().getMessagesPack();
        String activity = (String) params.get("param$activity");
        String transition = (String) params.get("param$transition");

        TextField outcomeText = getComponent("outcomeText");
        outcomeText.setValue(MessageProvider.getMessage(messagesPack, activity + (transition != null ? "." + transition : "")));
        outcomeText.setEditable(false);

        String commentRequired = (String) params.get("param$commentRequired");
        commentText.setRequired(commentRequired == null || Boolean.valueOf(commentRequired).equals(Boolean.TRUE));

        Component attachmentsPane = getComponent("attachmentsPane");
        String attachmentsVisible = (String) params.get("param$attachmentsVisible");
        attachmentsPane.setVisible(attachmentsVisible == null || Boolean.valueOf(attachmentsVisible).equals(Boolean.TRUE));

        TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable);
        attachmentsTH.createCreateAction(
                new ValueProvider() {
                    public Map<String, Object> getValues() {
                        Map<String, Object> values = new HashMap<String, Object>();
                        values.put("assignment", getDsContext().get("assignmentDs").getItem());
                        values.put("file", new FileDescriptor());
                        return values;
                    }
                    public Map<String, Object> getParameters() {
                        return Collections.emptyMap();
                    }
                },
                WindowManager.OpenType.DIALOG);
        attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG);
        attachmentsTH.createRemoveAction(false);

        Assignment assignment;

        if (activity.equals(WfConstants.ACTION_CANCEL)) {
            assignment = new Assignment();
            assignment.setName(WfConstants.CARD_STATE_CANCELED);
            assignment.setOutcome("Ok");
            UserSession userSession = UserSessionClient.getUserSession();
            assignment.setUser(userSession.getCurrentOrSubstitutedUser());
            assignment.setFinishedByUser(userSession.getUser());
            assignment.setCard(card);
            assignment.setProc(card.getProc());
        } else {
            LoadContext ctx = new LoadContext(Assignment.class);
            Object assignmentId = params.get("param$assignmentId");
            ctx.setId(assignmentId);
            ctx.setView("resolution-edit");
            assignment = ServiceLocator.getDataService().load(ctx);
        }

        getDsContext().get("assignmentDs").setItem(assignment);

        addAction(new AbstractAction("windowCommit") {

            public void actionPerform(Component component) {
                if (commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
                    showNotification(getMessage("putComments"), NotificationType.WARNING);
                } else {
                    getDsContext().commit();
                    close(COMMIT_ACTION_ID);
                }
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {

            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Cancel");
            }
        });
    }

    @Override
    public String getComment() {
        return commentText.getValue();
    }
}
