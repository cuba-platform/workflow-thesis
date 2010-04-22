/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 20.04.2010 12:11:14
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
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.ProcRole;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class TransitionForm extends AbstractForm {
    private TextField commentText;
    private Table attachmentsTable;

    protected Card card;
    private CardRolesFrame cardRolesFrame;
    private CollectionDatasource cardRolesDs;
    private DateField dueDate;
    private TextField outcomeText;
    protected boolean defaultNotifyByEmail = true;

    
    public TransitionForm(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        String dueDateRequired = (String)params.get("dueDateRequired");
        String commentRequired = (String) params.get("param$commentRequired");

        commentText = getComponent("commentText");
        attachmentsTable = getComponent("attachmentsTable");
        cardRolesFrame = getComponent("cardRolesFrame");
        dueDate = getComponent("dueDate");
        outcomeText = getComponent("outcomeText");

        card = (Card) params.get("param$card");
        if (cardRolesFrame != null) {
            cardRolesFrame.init();
            cardRolesFrame.setCard(card);
            cardRolesDs = getDsContext().get("cardRolesDs");
            cardRolesDs.addListener(new DsListenerAdapter() {
                @Override
                public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                    if (state == Datasource.State.VALID) {
                      cardRolesFrame.procChanged(card.getProc());
                        fillMissingRoles();
                    }
                }
            });
        }

        if (dueDate != null) {
            dueDate.setRequired(dueDateRequired != null && Boolean.valueOf(dueDateRequired).equals(Boolean.TRUE));
            Datasource varDs = getDsContext().get("varsDs");
            varDs.refresh();
        }

        if (commentText != null)
            commentText.setRequired(commentRequired != null || Boolean.valueOf(commentRequired).equals(Boolean.TRUE));

        if (attachmentsTable != null) {
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
        }

        String messagesPack = card.getProc().getMessagesPack();
        String activity = (String) params.get("param$activity");
        String transition = (String) params.get("param$transition");

        LoadContext ctx = new LoadContext(Assignment.class);
        Object assignmentId = params.get("param$assignmentId");
        //when starting process
        if (assignmentId != null) {
            ctx.setId(assignmentId);
            ctx.setView("resolution-edit");
            Assignment assignment = ServiceLocator.getDataService().load(ctx);
            getDsContext().get("assignmentDs").setItem(assignment);
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, activity + "." + transition));
        } else {
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, WfConstants.ACTION_START));
        }
        outcomeText.setEditable(false);

        addAction(new AbstractAction("windowCommit") {

            public void actionPerform(Component component) {
                doCommit();
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

    protected void doCommit() {
        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return;
        }
        if ((dueDate != null) && dueDate.isRequired() && (dueDate.getValue() == null)) {
            showNotification(getMessage("putDueDate"), NotificationType.WARNING);
            return;
        }
        if (cardRolesFrame != null) {
            Set<String> emptyRolesNames = getEmptyRolesNames();
            if (!emptyRolesNames.isEmpty()) {
                String message = "";
                for (String emptyRoleName : emptyRolesNames) {
                    message += MessageProvider.formatMessage(getClass(), "actorNotDefined.msg", emptyRoleName) + "<br/>";
                    showNotification(message, NotificationType.WARNING);
                }
            }
        }
//                getDsContext().commit();
        if (commentText != null)
            getDsContext().get("assignmentDs").commit();

        if (cardRolesFrame != null)
            cardRolesDs.commit();

        if (dueDate != null)
            getDsContext().get("varsDs").commit();
        close(COMMIT_ACTION_ID, true);
    }
    

    private void fillMissingRoles() {
        Set<String> requiredRolesCodes = getRequiredRolesCodes();
        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole)cardRolesDs.getItem(itemId);
            if (requiredRolesCodes.contains(cardRole.getCode())) {
                requiredRolesCodes.remove(cardRole.getCode());
            }
        }

        for (String roleCode : requiredRolesCodes) {
            cardRolesFrame.addProcActor(card.getProc(), roleCode, null, defaultNotifyByEmail);
        }
    }

    private Set<String> getEmptyRolesNames() {
        Set<String> emptyRolesNames = new HashSet<String>();
        Map<String, String> procRolesNames = new HashMap<String, String>();
        for (ProcRole procRole : card.getProc().getRoles()) {
            procRolesNames.put(procRole.getCode(), procRole.getName());
        }

        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole)cardRolesDs.getItem(itemId);
            if (cardRole.getUser() == null) {
                emptyRolesNames.add(procRolesNames.get(cardRole.getCode()));
            }
        }
        return emptyRolesNames;
    }

    protected Set<String> getRequiredRolesCodes() {
        return Collections.emptySet();
    }
    

    @Override
    public String getComment() {
        if (commentText != null)
            return commentText.getValue();
        else
            return null;
    }
}
