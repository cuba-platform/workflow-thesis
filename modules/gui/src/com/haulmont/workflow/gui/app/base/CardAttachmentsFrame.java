/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.app.core.file.FileDownloadHelper;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.gui.app.base.attachments.*;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class CardAttachmentsFrame extends AbstractFrame {

    @Inject
    protected Metadata metadata;

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected TimeSource timeSource;

    @Inject
    protected Messages messages;

    protected Datasource<Card> cardDs;
    protected Table attachmentsTable;
    protected AttachmentCreator attachmentCreator;
    protected FileUploadField fastUploadButton;
    protected UUID assignmentId;

    @Inject
    protected PopupButton createAttachBtn;

    private boolean generatedColumnInited = false;
    private boolean cardCommitCheckRequired = true;

    public void init() {
        init(new HashMap<String, Object>());
    }

    public void init(@Nullable Map<String, Object> params) {
        cardDs = getDsContext().get("cardDs");
        attachmentsTable = (Table) getComponent("attachmentsTable");
        assignmentId = (UUID) params.get("assignmentId");

        attachmentCreator = createAttachmentsCreator();
        initAttachmentActions(params);
        initAttachmentsTableStyleProvider();
        initAttachmentsDs();
        initFastUpload(params);
    }

    protected AttachmentCreator.CardAttachmentCreator createAttachmentsCreator() {
        return new AttachmentCreator.CardAttachmentCreator() {
            public Attachment createObject() {
                CardAttachment attachment = metadata.create(CardAttachment.class);
                Card card = cardDs.getItem();
                attachment.setCard(card.getFamilyTop());
                attachment.setCreatedBy(userSessionSource.getUserSession().getCurrentOrSubstitutedUser().getLogin());
                attachment.setCreateTs(timeSource.currentTimestamp());
                attachment.setSubstitutedCreator(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
                if (assignmentId != null) {
                    Assignment assignment = getDsContext().getDataSupplier().<Assignment>load(new
                            LoadContext(Assignment.class).setId(assignmentId));
                    attachment.setAssignment(assignment);
                }
                return attachment;
            }

            @Override
            public Card getCard() {
                return cardDs.getItem();
            }
        };
    }

    protected void initAttachmentsTableStyleProvider() {
        attachmentsTable.setStyleProvider(new Table.StyleProvider<Attachment>() {
            public String getStyleName(Attachment entity, String property) {
                return entity.getVersionOf() != null ? "grey" : null;
            }
        });
    }

    protected void initAttachmentActions(Map<String, Object> params) {
        initMultiUploadAction(params);
        initNewVersionAction(params);
        initEditAction(params);
        initRemoveAction(params);
        initCopyPasteActions(params);
        AttachmentActionsHelper.createLoadAction(attachmentsTable, this);
    }

    protected void initMultiUploadAction(Map<String, Object> params) {
        Action multiUploadAction = AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this,
                attachmentCreator, WindowManager.OpenType.DIALOG, params);

        createAttachBtn.addAction(multiUploadAction);
//        createPopup.addAction(new CommitCardAction("actions.MultiUpload", multiUploadAction));
    }

    protected void initNewVersionAction(Map<String, Object> params) {
        NewVersionAction newVersionAction = new NewVersionAction(attachmentsTable, WindowManager.OpenType.DIALOG) {
            @Override
            public Map<String, Object> getInitialValues() {
                Map<String, Object> initialValues = new HashMap<>();
                initialValues.put("card", cardDs.getItem());
                initialValues.put("file", new FileDescriptor());
                return initialValues;
            }
        };

        newVersionAction.setWindowParams(params);
        createAttachBtn.addAction(newVersionAction);
        attachmentsTable.addAction(newVersionAction);
    }

    protected void initEditAction(Map<String, Object> params) {
        EditAction editAction = new EditAction(attachmentsTable, WindowManager.OpenType.DIALOG, "edit") {
            @Override
            public void setEnabled(boolean enabled) {
                super.setEnabled(CardAttachmentsFrame.this.isEnabled() && enabled);
            }
        };
        Map<String, Object> map = params == null ? new HashMap<String, Object>() : params;
        map.put("edit", true);
        editAction.setWindowParams(map);
        attachmentsTable.addAction(editAction);
    }

    protected void initRemoveAction(Map<String, Object> params) {
        attachmentsTable.addAction(new RemoveAttachmentAction(attachmentsTable, new AttachmentCreator.CardGetter() {
            @Override
            public Card getCard() {
                return cardDs.getItem();
            }
        }, "remove"));
    }

    protected void initCopyPasteActions(Map<String, Object> params) {
        Button copyAttachBtn = (Button) getComponentNN("copyAttach");
        copyAttachBtn.setAction(AttachmentActionsHelper.createCopyAction(attachmentsTable));
        copyAttachBtn.setCaption(messages.getMessage(getClass(), AttachmentActionsHelper.COPY_ACTION_ID));

        Button pasteAttachBtn = (Button) getComponentNN("pasteAttach");
        Action pasteAction = AttachmentActionsHelper.createPasteAction(attachmentsTable, attachmentCreator, params);
        pasteAttachBtn.setAction(pasteAction);
//        pasteAttachBtn.setAction(new CommitCardAction(pasteAction.getId(), pasteAction));
        pasteAttachBtn.setCaption(messages.getMessage(getClass(), AttachmentActionsHelper.PASTE_ACTION_ID));

        attachmentsTable.addAction(copyAttachBtn.getAction());
        attachmentsTable.addAction(pasteAttachBtn.getAction());
    }

    protected void initFastUpload(Map<String, Object> excludedAttachTypes) {
        Label fastUpload = (Label) getComponent("fastUpload");
        BoxLayout fastUploadBox = (BoxLayout) getComponent("fastUploadBox");

        fastUploadButton = AttachmentActionsHelper.createFastUploadButton(attachmentsTable,
                attachmentCreator, "wf$CardAttachment.edit", excludedAttachTypes, WindowManager.OpenType.DIALOG);
        if (fastUploadBox != null) {
            fastUploadBox.remove(createAttachBtn);
            fastUploadBox.remove(fastUpload);
            fastUploadBox.add(fastUploadButton);
            fastUploadBox.add(createAttachBtn);
        }

    }

    protected void initAttachmentsDs() {
        CollectionDatasource attachmentDs = attachmentsTable.getDatasource();
        //noinspection unchecked
        attachmentDs.addStateChangeListener(e -> {
            if (!generatedColumnInited && e.getState() == Datasource.State.VALID) {
                FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");
                AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
                generatedColumnInited = true;
            }
        });

        CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.DESC);
        sortInfo.setPropertyPath(attachmentDs.getMetaClass().getPropertyPath("createTs"));
        ((CollectionDatasourceImpl) attachmentDs).sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});
        attachmentDs.refresh();
    }

    public FileUploadField getFastUploadButton() {
        return fastUploadButton;
    }

    public AttachmentCreator getAttachmentCreator() {
        return this.attachmentCreator;
    }

    public void setCardCommitCheckRequired(boolean cardCommitCheckRequired) {
        this.cardCommitCheckRequired = cardCommitCheckRequired;
    }

    public boolean isCardCommitCheckRequired() {
        return cardCommitCheckRequired;
    }

//    protected class CommitCardAction extends AbstractAction {
//
//        private Action afterPerformAction;
//
//        public CommitCardAction(String id, Action afterPerformAction) {
//            super(id);
//            this.afterPerformAction = afterPerformAction;
//        }
//
//        public void actionPerform(Component component) {
//            if (!PersistenceHelper.isNew(cardDs.getItem()) && cardCommitCheckRequired) {
//                showOptionDialog(
//                        getMessage("cardAttachmentFrame.dialogHeader"),
//                        getMessage("cardAttachmentFrame.dialogMessage"),
//                        Frame.MessageType.CONFIRMATION,
//                        new Action[]{
//                                new DialogAction(DialogAction.Type.YES) {
//                                    @Override
//                                    public void actionPerform(Component component) {
//                                        ((DatasourceImpl) getDsContext().get("cardDs")).setModified(true);
//                                        boolean isCommited = true;
//                                        if (getFrame() instanceof WebWindow.Editor)
//                                            isCommited = ((AbstractEditor) ((WebWindow.Editor) getFrame()).getWrapper()).commit();
//                                        else
//                                            getFrame().getDsContext().commit();
//
//                                        if (isCommited)
//                                            afterPerformAction.actionPerform(null);
//                                    }
//                                },
//                                new DialogAction(DialogAction.Type.NO)
//                        });
//            } else {
//                afterPerformAction.actionPerform(null);
//            }
//        }
//    }
}
