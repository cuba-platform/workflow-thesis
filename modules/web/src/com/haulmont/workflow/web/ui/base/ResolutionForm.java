/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
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
import com.haulmont.workflow.core.global.WfConfig;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.web.ui.base.action.AbstractForm;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ResolutionForm extends AbstractForm {

    private TextArea commentText;
    private Table attachmentsTable;
    private Assignment assignment;
    protected AttachmentType attachmentType;

    private CollectionDatasourceImpl<Assignment, UUID> datasource;
    protected Map<Card, AssignmentInfo> cardAssignmentInfoMap;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);

        commentText = getComponent("commentText");
        attachmentsTable = getComponent("attachmentsTable");

        final Card card = (Card) params.get("param$card");
        String messagesPack = card.getProc().getMessagesPack();
        String activity = (String) params.get("param$activity");
        String transition = (String) params.get("param$transition");

        TextField outcomeText = getComponent("outcomeText");
        outcomeText.setValue(messages.getMessage(messagesPack, activity + (transition != null ? "." + transition : "")));
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
                    CommitContext commitContext = new CommitContext();
                    commitContext.getCommitInstances().addAll(copyAttachments());
                    getDsContext().getDataService().commit(commitContext);
                    getDsContext().commit();
                    onCommit();
                    close(COMMIT_ACTION_ID);
                }
            }

            @Override
            public String getCaption() {
                return MessageProvider.getMessage(AppConfig.getMessagesPack(), "actions.Ok");
            }
        });

        addAction(new AbstractAction("windowClose") {

            public void actionPerform(Component component) {
                close("cancel", true);
            }

            @Override
            public String getCaption() {
                return messages.getMessage(AppConfig.getMessagesPack(), "actions.Cancel");
            }
        });

        final CollectionDatasource assignmentDs = getDsContext().get("assignmentDs");
        // Add attachments handler
        Button copyAttachBtn = getComponent("copyAttach");
        copyAttachBtn.setAction(AttachmentActionsHelper.createCopyAction(attachmentsTable));
        copyAttachBtn.setCaption(messages.getMessage(getClass(), "actions.Copy"));

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
        pasteAttachBtn.setCaption(messages.getMessage(getClass(), "actions.Paste"));

        PopupButton createPopup = getComponent("createAttachBtn");
        TableActionsHelper helper = new TableActionsHelper(this, attachmentsTable);
        WfConfig wfConfig = ConfigProvider.getConfig(WfConfig.class);
        if (wfConfig.getOneAttachmentUploaderEnabled()) {
            createPopup.addAction(helper.createCreateAction(
                    new ValueProvider() {
                        public Map<String, Object> getValues() {
                            Map<String, Object> values = new HashMap<String, Object>();
                            values.put("assignment", getDsContext().get("assignmentDs").getItem());
                            values.put("file", new FileDescriptor());
                            values.put("card", card);
                            if (attachmentType != null) {
                                values.put("attachType", attachmentType);
                            }
                            return values;
                        }

                        public Map<String, Object> getParameters() {
                            return Collections.emptyMap();
                        }
                    },
                    WindowManager.OpenType.DIALOG, "actions.New"
            ));
        }

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

            datasource.addListener(new DsListenerAdapter<Assignment>() {
                @Override
                public void valueChanged(Assignment source, String property, Object prevValue, Object value) {
                    if (source.equals(assignment)) {
                        for (Object key : datasource.getItemIds()) {
                            InstanceUtils.setValueEx(datasource.getItem((UUID) key), property, value);
                        }
                    }
                }
            });
        }
    }

    protected List<CardAttachment> copyAttachments() {
        List<CardAttachment> attachmentList = datasource.getItem().getAttachments();
        List<CardAttachment> commitList = new ArrayList<>();
        if (datasource.getItemIds().size() > 1 && attachmentList != null) {
            for (Object key : datasource.getItemIds()) {
                if (key.equals(assignment.getId())) {
                    continue;
                }

                Assignment item = datasource.getItem((UUID) key);
                List<CardAttachment> copyAttachmentList = new ArrayList<>();
                for (CardAttachment attachment : attachmentList) {
                    CardAttachment cardAttachment = MetadataProvider.create(CardAttachment.class);
                    cardAttachment.setAssignment(item);
                    cardAttachment.setCard(item.getCard().getFamilyTop());
                    cardAttachment.setFile(attachment.getFile());
                    cardAttachment.setName(attachment.getName());
                    copyAttachmentList.add(cardAttachment);
                    commitList.add(cardAttachment);
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