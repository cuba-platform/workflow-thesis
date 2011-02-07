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
import com.haulmont.cuba.core.global.MessageUtils;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.List;

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

    private String requiredRolesCodes;

    public TransitionForm(IFrame frame) {
        super(frame);
    }

    @Override
    protected void init(Map<String, Object> params) {
        super.init(params);

        String dueDateRequired = (String) params.get("dueDateRequired");
        String commentRequired = (String) params.get("param$commentRequired");
        requiredRolesCodes = (String) params.get("param$requiredRoles");

        String additionalRolesCodes = (String) params.get("param$additionalRoles");
        if (!StringUtils.isEmpty(additionalRolesCodes)) {
            requiredRolesCodes += "," + additionalRolesCodes;
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
                        fillMissingRoles();
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
                    AssignmentAttachment attachment = new AssignmentAttachment();
                    attachment.setAssignment((Assignment) assignmentDs.getItem());
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
                dueDate.setDateFormat(MessageUtils.getDateTimeFormat());
            }
        }

        addAction(new AbstractAction("windowCommit") {

            public void actionPerform(Component component) {
                if (doCommit())
                    close(COMMIT_ACTION_ID, true);
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

        if (commentText != null)
            commentText.setRequired(commentRequired != null && Boolean.valueOf(commentRequired).equals(Boolean.TRUE));
        if (attachmentsTable != null)
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
    }

    protected boolean doCommit() {
        if (!validate()) return false;
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

    protected boolean validate() {
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
            Set<String> emptyRolesNames = getEmptyRolesNames();
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

    private void fillMissingRoles() {
        Set<String> requiredRolesCodes = getRequiredRolesCodes(cardRolesDs.getItemIds().size() == 0);
        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole) cardRolesDs.getItem(itemId);
            requiredRolesCodes.remove(cardRole.getCode());
        }

        Proc proc = card.getProc();
        proc = cardRolesDs.getDataService().reload(proc, "start-process");

        for (String roleCode : requiredRolesCodes) {
            if (!roleCode.contains("|"))
                cardRolesFrame.addProcActor(proc, roleCode, null, defaultNotifyByEmail);
        }
    }

    private Set<String> getEmptyRolesNames() {
        Set<String> emptyRolesNames = new HashSet<String>();
        Map<String, String> procRolesNames = new HashMap<String, String>();
        List<ProcRole> procRoles = card.getProc().getRoles();
        if (procRoles == null) {
            LoadContext ctx = new LoadContext(ProcRole.class);
            LoadContext.Query query = ctx.setQueryString("select pr from wf$ProcRole pr where pr.proc.id = :proc");
            query.addParameter("proc", card.getProc());
            procRoles = ServiceLocator.getDataService().loadList(ctx);
        }
        for (ProcRole procRole : procRoles) {
            procRolesNames.put(procRole.getCode(), procRole.getName());
        }

        //if we removed required role from datasource
        Set<String> emptyRequiredRolesCodes = getRequiredRolesCodes(false);

        Set<String> requiredRolesChoiceCodes = new HashSet<String>();
        for (String requiredRoleCode : emptyRequiredRolesCodes) {
            if (requiredRoleCode.contains("|"))
                requiredRolesChoiceCodes.add(requiredRoleCode);
        }

        for (Object itemId : cardRolesDs.getItemIds()) {
            CardRole cardRole = (CardRole) cardRolesDs.getItem(itemId);
            if (cardRole.getUser() == null) {
                emptyRolesNames.add(procRolesNames.get(cardRole.getCode()));
            }

            if (!requiredRolesChoiceCodes.isEmpty()) {
                String choiceRole = null;
                for (String requiredRolesChoiceCode : requiredRolesChoiceCodes) {
                    String[] roles = requiredRolesChoiceCode.split("\\|");
                    if (Arrays.binarySearch(roles, cardRole.getCode()) >= 0) {
                        choiceRole = requiredRolesChoiceCode;
                        break;
                    }
                }

                if (choiceRole != null) {
                    requiredRolesChoiceCodes.remove(choiceRole);
                    emptyRequiredRolesCodes.remove(choiceRole);
                }
            }

            emptyRequiredRolesCodes.remove(cardRole.getCode());
        }

        for (String roleCode : emptyRequiredRolesCodes) {
            if (roleCode.contains("|")) {
                String formattingCode = "";
                String orStr = " " + MessageProvider.getMessage(TransitionForm.class, "actorNotDefined.or") + " ";
                String[] roles = roleCode.split("\\|");
                for (String role : roles) {
                    formattingCode += procRolesNames.get(role) + orStr;
                }

                if (formattingCode.endsWith(orStr))
                    formattingCode = formattingCode.substring(0, formattingCode.lastIndexOf(orStr));

                emptyRolesNames.add(formattingCode);
            } else {
                emptyRolesNames.add(procRolesNames.get(roleCode));
            }
        }

        return emptyRolesNames;
    }

    protected Set<String> getRequiredRolesCodes(boolean isAll) {
        if (requiredRolesCodes != null) {
            String[] s = requiredRolesCodes.split(isAll ? "\\s*[,|]\\s*" : "\\s*,\\s*");
            return new LinkedHashSet<String>(Arrays.asList(s));
        }
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
