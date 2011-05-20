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

import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.CommitContext;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.List;

public class ResolutionForm extends AbstractForm {

    private TextField commentText;
    private Table attachmentsTable;
    private Assignment assignment;
    protected AttachmentType attachmentType;

    private CollectionDatasourceImpl<Assignment, UUID> datasource;
    protected Map<Card, AssignmentInfo> cardAssignmentInfoMap;

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
        attachmentsTH.createEditAction(WindowManager.OpenType.DIALOG);
        attachmentsTH.createRemoveAction(false);

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
            Object assignmentId = params.get("param$assignmentId");
            if (assignmentId == null) {
                throw new RuntimeException(getMessage("resolutionFormFailed"));
            }
            assignment = reloadAssignment(assignmentId);
        }

        datasource = getDsContext().get("assignmentDs");
        datasource.valid();
        datasource.setItem(assignment);
        applyToCards();

        addAction(new AbstractAction("windowCommit") {

            public void actionPerform(Component component) {
                if (commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
                    showNotification(getMessage("putComments"), NotificationType.WARNING);
                } else {
                    CommitContext<Entity> commitContext = new CommitContext<Entity>();
                    commitContext.getCommitInstances().addAll(copyAttachments());
                    getDsContext().getDataService().commit(commitContext);
                    getDsContext().commit();
                    onCommit();
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
                close("cancel", true);
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getInstance().getMessagesPack(), "actions.Cancel");
            }
        });

        final CollectionDatasource assignmentDs = getDsContext().get("assignmentDs");
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

        PopupButton createPopup = getComponent("createAttachBtn");
        TableActionsHelper helper = new TableActionsHelper(this, attachmentsTable);
        createPopup.addAction(helper.createCreateAction(
                new ValueProvider() {
                    public Map<String, Object> getValues() {
                        Map<String, Object> values = new HashMap<String, Object>();
                        values.put("assignment", getDsContext().get("assignmentDs").getItem());
                        values.put("file", new FileDescriptor());
                        if (attachmentType != null){
                            values.put("attachType", attachmentType);
                        }
                        return values;
                    }

                    public Map<String, Object> getParameters() {
                        return Collections.emptyMap();
                    }
                },
                WindowManager.OpenType.DIALOG,"actions.New"
        ));

        Map map = new HashMap<String, Object>();
        if (attachmentType != null) {
            map.put("attachType", attachmentType);
        }
        createPopup.addAction(AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this, creator, WindowManager.OpenType.DIALOG, map));

        attachmentsTable.addAction(copyAttachBtn.getAction());
        attachmentsTable.addAction(pasteAttachBtn.getAction());
        AttachmentActionsHelper.createLoadAction(attachmentsTable, this);
        if (attachmentsTable != null)
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
    }

    protected void onCommit() {
    }

    protected void applyToCards() {
        cardAssignmentInfoMap = getContext().getParamValue("cardAssignmentInfoMap");
        if (cardAssignmentInfoMap != null) {
            for (AssignmentInfo assignmentInfo : cardAssignmentInfoMap.values()) {
                Assignment assign = assignment.getId().equals(assignmentInfo.getAssignmentId()) ?
                        datasource.getItem() : reloadAssignment(assignmentInfo.getAssignmentId());
                datasource.addItem(assign);
            }

            datasource.addListener(new DsListenerAdapter() {
                @Override
                public void valueChanged(Entity source, String property, Object prevValue, Object value) {
                    if (source.equals(assignment)) {
                        for (Object key : datasource.getItemIds()) {
                            InstanceUtils.setValueEx(datasource.getItem((UUID) key), property, value);
                        }
                    }
                }
            });
        }
    }

    protected List<AssignmentAttachment> copyAttachments() {
        List<AssignmentAttachment> attachmentList = datasource.getItem().getAttachments();
        List<AssignmentAttachment> commitList = new ArrayList<AssignmentAttachment>();
        if (datasource.getItemIds().size() > 1 && attachmentList != null) {
            for (Object key : datasource.getItemIds()) {
                if (key.equals(assignment.getId())) {
                    continue;
                }

                Assignment item = datasource.getItem((UUID) key);
                List<AssignmentAttachment> copyAttachmentList = new ArrayList<AssignmentAttachment>();
                for (AssignmentAttachment attachment : attachmentList) {
                    AssignmentAttachment assignmentAttachment = new AssignmentAttachment();
                    assignmentAttachment.setAssignment(item);
                    assignmentAttachment.setFile(attachment.getFile());
                    assignmentAttachment.setName(attachment.getName());
                    copyAttachmentList.add(assignmentAttachment);
                    commitList.add(assignmentAttachment);
                }
                item.setAttachments(copyAttachmentList);
            }
        }
        return commitList;
    }

    private Assignment reloadAssignment(Object id) {
        LoadContext ctx = new LoadContext(Assignment.class);
        ctx.setId(id);
        ctx.setView("resolution-edit");
        return ServiceLocator.getDataService().load(ctx);
    }

    @Override
    public String getComment() {
        return commentText.getValue();
    }
}
