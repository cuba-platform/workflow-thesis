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

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class TransitionForm extends AbstractForm {
    private TextField commentText;
    private Table attachmentsTable;

    protected Card card;
    protected CardRolesFrame cardRolesFrame;
    protected CollectionDatasource cardRolesDs;
    protected Datasource assignmentDs;
    private DateField dueDate;
    private TextField outcomeText;
    protected boolean defaultNotifyByEmail = true;

    private String requiredRolesCodesStr;

    public TransitionForm(IFrame frame) {
        super(frame);
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        String dueDateRequired = (String) params.get("dueDateRequired");
        String commentRequired = (String) params.get("param$commentRequired");
        requiredRolesCodesStr = (String) params.get("param$requiredRoles");

        String additionalRolesCodes = (String) params.get("param$additionalRoles");
        if (!StringUtils.isEmpty(additionalRolesCodes)) {
            requiredRolesCodesStr += "," + additionalRolesCodes;
        }

        commentText = getComponent("commentText");
        attachmentsTable = getComponent("attachmentsTable");
        cardRolesFrame = getComponent("cardRolesFrame");
        dueDate = getComponent("dueDate");
        outcomeText = getComponent("outcomeText");

        assignmentDs = getDsContext().get("assignmentDs");

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
                        cardRolesFrame.setRequiredRolesCodesStr(requiredRolesCodesStr);
                        cardRolesFrame.fillMissingRoles();
                    }
                }
            });
            cardRolesDs.refresh();
        }

        if (dueDate != null) {
            dueDate.setRequired(dueDateRequired != null && Boolean.valueOf(dueDateRequired).equals(Boolean.TRUE));
            Datasource varDs = getDsContext().get("varsDs");
            varDs.refresh();
        }

        if (attachmentsTable != null) {
            TableActionsHelper attachmentsTH = new TableActionsHelper(this, attachmentsTable);
            attachmentsTH.createCreateAction(
                    new ValueProvider() {
                        public Map<String, Object> getValues() {
                            Map<String, Object> values = new HashMap<String, Object>();
                            values.put("assignment", assignmentDs.getItem());
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

            // Add attachments handler
            Button copyAttachBtn = getComponent("copyAttach");
            copyAttachBtn.setAction(AttachmentActionsHelper.createCopyAction(attachmentsTable));
            copyAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Copy"));

            Button pasteAttachBtn = getComponent("pasteAttach");
            AttachmentCreator creator = new AttachmentCreator() {
                public Attachment createObject() {
                    CardAttachment attachment = MetadataProvider.create(CardAttachment.class);
                    attachment.setAssignment((Assignment) assignmentDs.getItem());
                    attachment.setCard(((Assignment) assignmentDs.getItem()).getCard());
                    return attachment;
                }
            };
            pasteAttachBtn.setAction(
                    AttachmentActionsHelper.createPasteAction(attachmentsTable, creator));
            pasteAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Paste"));
            Button uploadManyBtn = getComponent("uploadMany");
            uploadManyBtn.setAction(AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this, creator));

            attachmentsTable.addAction(uploadManyBtn.getAction());
            attachmentsTable.addAction(copyAttachBtn.getAction());
            attachmentsTable.addAction(pasteAttachBtn.getAction());
            AttachmentActionsHelper.createLoadAction(attachmentsTable, this);
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
            assignmentDs.setItem(assignment);
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, activity + "." + transition));
            if (commentText != null)
                commentText.setDatasource(assignmentDs, "comment");
        } else {
            outcomeText.setValue(MessageProvider.getMessage(messagesPack, WfConstants.ACTION_START));
        }
        outcomeText.setEditable(false);

        String formCaption = (String) params.get("param$formCaption");
        if (StringUtils.isNotBlank(formCaption))
            setCaption(MessageProvider.getMessage(messagesPack, formCaption));

        String dueDateLabelParam = (String) params.get("param$dueDateLabel");
        if (StringUtils.isNotBlank(dueDateLabelParam)) {
            Label dueDateLabel = getComponent("dueDateLabel");
            if (dueDateLabel != null)
                dueDateLabel.setValue(MessageProvider.getMessage(messagesPack, dueDateLabelParam));
        }

        String dueDateFormatParam = (String) params.get("param$dueDateFormat");
        if (StringUtils.isNotBlank(dueDateFormatParam)) {
            if ("dateTimeFormat".equals(dueDateFormatParam)) {
                dueDate.setResolution(DateField.Resolution.MIN);
                dueDate.setDateFormat(Datatypes.getFormatStrings(UserSessionProvider.getLocale()).getDateTimeFormat());
            }
        }

        addAction(new AbstractAction("windowCommit") {

            public void actionPerform(Component component) {
                if (doCommit())
                    close(COMMIT_ACTION_ID, true);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {

            public void actionPerform(Component component) {
                close("cancel");
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        if (commentText != null)
            commentText.setRequired(commentRequired != null && Boolean.valueOf(commentRequired).equals(Boolean.TRUE));
        if (attachmentsTable != null)
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
    }

    protected boolean doCommit() {
        if (!validated()) return false;
//                getDsContext().commit();
        if (commentText != null) {
            if (Datasource.State.VALID.equals(assignmentDs.getState()))
                assignmentDs.commit();
            else {
                if (card.getInitialProcessVariables() == null) {
                    card.setInitialProcessVariables(new HashMap<String, Object>(1));
                }
                card.getInitialProcessVariables().put("startProcessComment", commentText.getValue());
            }
        }

        if (cardRolesFrame != null)
            cardRolesDs.commit();

        if (dueDate != null)
            getDsContext().get("varsDs").commit();

        return true;
    }

    protected boolean validated() {
        if (commentText != null && commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
            return false;
        }
        if ((dueDate != null) && (dueDate.getValue() != null) && (((Date) dueDate.getValue()).compareTo(TimeProvider.currentTimestamp()) < 0)) {
            showNotification(getMessage("dueDateIsLessThanNow"), NotificationType.WARNING);
            return false;
        }
        if ((dueDate != null) && dueDate.isRequired() && (dueDate.getValue() == null)) {
            showNotification(getMessage("putDueDate"), NotificationType.WARNING);
            return false;
        }
        if (cardRolesFrame != null) {
            Set<String> emptyRolesNames = cardRolesFrame.getEmptyRolesNames();
            if (!emptyRolesNames.isEmpty()) {
                String message = "";
                for (String emptyRoleName : emptyRolesNames) {
                    message += MessageProvider.formatMessage(TransitionForm.class, "actorNotDefined.msg", emptyRoleName) + "<br/>";
                }
                showNotification(message, NotificationType.WARNING);
                return false;
            }
        }
        return true;
    }

    @Override
    public String getComment() {
        if (commentText != null)
            return commentText.getValue();
        else
            return null;
    }
}
