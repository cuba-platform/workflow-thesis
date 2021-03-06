/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base;

import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.security.global.UserSession;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.AssignmentInfo;
import com.haulmont.workflow.core.global.WfConfig;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.app.attachment.ProcessAttachmentsManager;
import com.haulmont.workflow.gui.app.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.gui.app.base.attachments.AttachmentCreator;
import com.haulmont.workflow.gui.app.tools.AttachmentActionTools;
import com.haulmont.workflow.gui.base.action.AbstractForm;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author krivopustov
 * @version $Id$
 */
public class ResolutionForm extends AbstractForm {

    @Inject
    protected TextArea commentText;

    @Inject
    protected Table attachmentsTable;

    @Inject
    protected CollectionDatasourceImpl<Assignment, UUID> assignmentDs;

    protected Map<Card, AssignmentInfo> cardAssignmentInfoMap;

    @Inject
    protected Messages messages;

    @Inject
    protected Metadata metadata;

    @Inject
    protected UserSession userSession;

    @Inject
    protected AttachmentActionTools attachmentActionTools;

    @Inject
    protected ProcessAttachmentsManager processAttachments;

    @WindowParam
    protected String initHeight;
    protected int defaultInitHeight = 420;

    protected Assignment assignment;

    protected AttachmentType attachmentType;

    @Override
    public void init(final Map<String, Object> params) {
        super.init(params);

        initDialogParams(params);

        final Card card = (Card) params.get("card");
        Card procContextCard = (Card) params.get("procContextCard");
        if (procContextCard == null) procContextCard = card;

        String messagesPack = procContextCard.getProc().getMessagesPack();
        String activity = (String) params.get("activity");
        String transition = (String) params.get("transition");

        TextField outcomeText = getComponent("outcomeText");
        outcomeText.setValue(messages.getMessage(messagesPack, activity + (transition != null ? "." + transition : "")));
        outcomeText.setEditable(false);

        String commentRequired = (String) params.get("commentRequired");
        commentText.setRequired(commentRequired == null || Boolean.valueOf(commentRequired).equals(Boolean.TRUE));

        Component attachmentsPane = getComponent("attachmentsPane");
        String attachmentsVisible = (String) params.get("attachmentsVisible");
        attachmentsPane.setVisible(attachmentsVisible == null || Boolean.valueOf(attachmentsVisible).equals(Boolean.TRUE));

        attachmentsTable.addAction(new EditAction(attachmentsTable, WindowManager.OpenType.DIALOG));
        attachmentsTable.addAction(new RemoveAction(attachmentsTable, false));

        if (activity.equals(WfConstants.ACTION_CANCEL)) {
            assignment = metadata.create(Assignment.class);
            assignment.setName(WfConstants.CARD_STATE_CANCELED);
            assignment.setOutcome("Ok");
            assignment.setUser(userSession.getCurrentOrSubstitutedUser());
            assignment.setFinishedByUser(userSession.getUser());
            assignment.setCard(card);
            assignment.setProc(card.getProc());
        } else {
            Object assignmentId = params.get("assignmentId");
            if (assignmentId == null) {
                throw new RuntimeException(getMessage("resolutionFormFailed"));
            }
            assignment = reloadAssignment(assignmentId);
        }

        assignmentDs.valid();
        assignmentDs.addItem(assignment);
        assignmentDs.setItem(assignment);
        applyToCards();

        // Add attachments handler
        Button copyAttachBtn = getComponent("copyAttach");
        copyAttachBtn.setAction(attachmentActionTools.createCopyAction(attachmentsTable));
        copyAttachBtn.setCaption(messages.getMessage(getClass(), "actions.Copy"));

        Button pasteAttachBtn = getComponent("pasteAttach");
        AttachmentCreator creator = createAttachmentCreator();
        pasteAttachBtn.setAction(
                attachmentActionTools.createPasteAction(attachmentsTable, creator, getPasteActionParams()));
        pasteAttachBtn.setCaption(messages.getMessage(getClass(), "actions.Paste"));

        PopupButton createPopup = getComponent("createAttachBtn");
        WfConfig wfConfig = AppBeans.get(Configuration.class).getConfig(WfConfig.class);
        if (wfConfig.getOneAttachmentUploaderEnabled()) {
            createPopup.addAction(new CreateAction(attachmentsTable, WindowManager.OpenType.DIALOG, "actions.New") {
                @Override
                public Map<String, Object> getInitialValues() {
                    Map<String, Object> values = new HashMap<>();
                    values.put("assignment", assignmentDs.getItem());
                    values.put("file", metadata.create(FileDescriptor.class));
                    values.put("card", card);
                    if (attachmentType != null) {
                        values.put("attachType", attachmentType);
                    }
                    return values;
                }
            });
        }

        createPopup.addAction(attachmentActionTools.createMultiUploadAction(
                attachmentsTable, this, creator, WindowManager.OpenType.DIALOG, getMultiUploadActionParams()));

        attachmentsTable.addAction(copyAttachBtn.getAction());
        attachmentsTable.addAction(pasteAttachBtn.getAction());
        attachmentActionTools.createLoadAction(attachmentsTable, this);
        if (attachmentsTable != null)
            AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
    }

    protected Map<String, Object> getPasteActionParams() {
        return null;
    }

    protected Map<String, Object> getMultiUploadActionParams() {
        Map<String, Object> map = new HashMap<>();
        if (attachmentType != null) {
            map.put("attachType", attachmentType);
        }
        return map;
    }

    protected void initDialogParams(Map<String, Object> params) {
        if (initHeight != null) {
            getDialogParams().setHeight(Integer.parseInt(initHeight));
        } else {
            String attachmentsVisible = (String) params.get("attachmentsVisible");
            if (attachmentsVisible == null || Boolean.valueOf(attachmentsVisible).equals(Boolean.TRUE))
                getDialogParams().setHeight(defaultInitHeight);
        }
    }

    @Override
    protected void onWindowCommit() {
        if (!PersistenceHelper.isNew(assignment)) {
            Assignment reloaded = getDsContext().getDataSupplier().reload(assignment,
                    metadata.getViewRepository().getView(Assignment.class, "resolution-edit"),
                    metadata.getClass(Assignment.class), false);
            if (reloaded.getFinished() != null) {
                showNotification(messages.getMessage("com.haulmont.workflow.gui.base", "optimisticLockMessage"), NotificationType.HUMANIZED);
                return;
            }
        }
        Card card = getDsContext().getDataSupplier().reload(assignment.getCard(),
                metadata.getViewRepository().getView(Card.class, View.MINIMAL),
                metadata.getClass(Card.class), false);
        /* If card was modified, initiate Optimistic Lock */
        if (card.getVersion().compareTo(assignment.getCard().getVersion()) != 0) {
            getDsContext().getDataSupplier().commit(new CommitContext(assignment.getCard()));
        }
        if (commentText.isRequired() && StringUtils.isBlank((String) commentText.getValue())) {
            showNotification(getMessage("putComments"), NotificationType.WARNING);
        } else {
            commitAndClose();
        }
    }

    protected void commitAndClose() {
        commitAttachments();
        onCommit();
        close(COMMIT_ACTION_ID);
    }

    @Override
    protected void onWindowClose() {
        close("cancel", true);
    }

    protected void onCommit() {
    }

    protected void applyToCards() {
        cardAssignmentInfoMap = getContext().getParamValue("cardAssignmentInfoMap");
        if (cardAssignmentInfoMap != null) {
            for (AssignmentInfo assignmentInfo : cardAssignmentInfoMap.values()) {
                Assignment assign = assignment.getId().equals(assignmentInfo.getAssignmentId()) ?
                        assignmentDs.getItem() : reloadAssignment(assignmentInfo.getAssignmentId());
                assignmentDs.addItem(assign);
            }

            assignmentDs.addListener(new DsListenerAdapter<Assignment>() {
                @Override
                public void valueChanged(Assignment source, String property, Object prevValue, Object value) {
                    if (source.equals(assignment)) {
                        for (Object key : assignmentDs.getItemIds()) {
                            InstanceUtils.setValueEx(assignmentDs.getItem((UUID) key), property, value);
                        }
                    }
                }
            });
        }
    }

    private Assignment reloadAssignment(Object id) {
        LoadContext ctx = new LoadContext(Assignment.class);
        ctx.setId(id);
        ctx.setView("resolution-edit");
        return getDsContext().getDataSupplier().load(ctx);
    }

    @Override
    public String getComment() {
        return commentText.getValue();
    }

    public void commitAttachments() {
        CommitContext commitContext = new CommitContext();
        commitContext.getCommitInstances().addAll(processAttachments.copyAttachments(assignmentDs.getItems()));
        getDsContext().getDataSupplier().commit(commitContext);
        getDsContext().commit();
    }

    protected AttachmentCreator createAttachmentCreator() {
        return new ResolutionAttachmentCreator();
    }

    public class ResolutionAttachmentCreator implements AttachmentCreator {

        @Override
        public Attachment createObject() {
            CardAttachment attachment = metadata.create(CardAttachment.class);
            attachment.setAssignment(assignmentDs.getItem());
            attachment.setCard(assignmentDs.getItem().getCard());
            return attachment;
        }
    }
}