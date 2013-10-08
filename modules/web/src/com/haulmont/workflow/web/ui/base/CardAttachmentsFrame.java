/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.cuba.web.gui.components.WebComponentsHelper;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.web.ui.base.attachments.*;
import com.vaadin.ui.Layout;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

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

    protected Datasource<Card> cardDs;
    protected Table attachmentsTable;
    protected AttachmentCreator attachmentCreator;
    protected FileUploadField fastUploadButton;

    private boolean generatedColumnInited = false;
    private boolean cardCommitCheckRequired = true;

    public void init() {
        init(new HashMap<String, Object>());
    }

    public void init(@Nullable Map<String, Object> params) {
        cardDs = getDsContext().get("cardDs");
        attachmentsTable = getComponent("attachmentsTable");


        attachmentCreator = new AttachmentCreator.CardAttachmentCreator() {
            public Attachment createObject() {
                CardAttachment attachment = metadata.create(CardAttachment.class);
                Card card = cardDs.getItem();
                attachment.setCard(card.getFamilyTop());
                attachment.setCreatedBy(userSessionSource.getUserSession().getCurrentOrSubstitutedUser().getLogin());
                attachment.setCreateTs(timeSource.currentTimestamp());
                attachment.setSubstitutedCreator(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
                return attachment;
            }

            @Override
            public Card getCard() {
                return cardDs.getItem();
            }
        };

        PopupButton createPopup = getComponent("createAttachBtn");
        Action multiUploadAction = AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this,
                attachmentCreator, WindowManager.OpenType.DIALOG, params);

        createPopup.addAction(multiUploadAction);
//        createPopup.addAction(new CommitCardAction("actions.MultiUpload", multiUploadAction));

        NewVersionAction newVersionAction = new NewVersionAction(attachmentsTable, WindowManager.OpenType.DIALOG) {
            @Override
            public Map<String, Object> getInitialValues() {
                Map<String, Object> initialValues = new HashMap();
                initialValues.put("card", cardDs.getItem());
                initialValues.put("file", new FileDescriptor());
                return initialValues;
            }
        };

        newVersionAction.setWindowParams(params);
        createPopup.addAction(newVersionAction);

        attachmentsTable.addAction(newVersionAction);
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
        attachmentsTable.addAction(new RemoveAttachmentAction(attachmentsTable, new AttachmentCreator.CardGetter() {
            @Override
            public Card getCard() {
                return cardDs.getItem();
            }
        }, "remove"));

        Button copyAttachBtn = getComponent("copyAttach");
        copyAttachBtn.setAction(AttachmentActionsHelper.createCopyAction(attachmentsTable));
        copyAttachBtn.setCaption(MessageProvider.getMessage(getClass(), AttachmentActionsHelper.COPY_ACTION_ID));

        Button pasteAttachBtn = getComponent("pasteAttach");
        Action pasteAction = AttachmentActionsHelper.createPasteAction(attachmentsTable, attachmentCreator, params);
        pasteAttachBtn.setAction(pasteAction);
//        pasteAttachBtn.setAction(new CommitCardAction(pasteAction.getId(), pasteAction));
        pasteAttachBtn.setCaption(MessageProvider.getMessage(getClass(), AttachmentActionsHelper.PASTE_ACTION_ID));

        attachmentsTable.addAction(copyAttachBtn.getAction());
        attachmentsTable.addAction(pasteAttachBtn.getAction());
        AttachmentActionsHelper.createLoadAction(attachmentsTable, this);

        CollectionDatasource attachmentDs = attachmentsTable.getDatasource();
        attachmentDs.addListener(new DsListenerAdapter() {
            @Override
            public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                if (!generatedColumnInited && state == Datasource.State.VALID) {
                    FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");
                    AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
                    generatedColumnInited = true;
                }
            }
        });

        attachmentsTable.setStyleProvider(new Table.StyleProvider<Attachment>() {
            public String getStyleName(Attachment entity, String property) {
                return entity.getVersionOf() != null ? "grey" : null;
            }
        });

        CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.DESC);
        sortInfo.setPropertyPath(attachmentDs.getMetaClass().getPropertyPath("createTs"));
        ((CollectionDatasourceImpl) attachmentDs).sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});

        attachmentDs.refresh();

        initFastUpload(params);

    }

    private void initFastUpload(Map<String, Object> excludedAttachTypes) {
        Label fastUpload = getComponent("fastUpload");
        com.vaadin.ui.Component parent = WebComponentsHelper.unwrap(fastUpload).getParent();

        fastUploadButton = AttachmentActionsHelper.createFastUploadButton(attachmentsTable,
                attachmentCreator, "wf$CardAttachment.edit", excludedAttachTypes, WindowManager.OpenType.DIALOG);
        if (parent != null) {
            ((Layout) parent).replaceComponent(WebComponentsHelper.unwrap(fastUpload), WebComponentsHelper.unwrap(fastUploadButton));
        }

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
//                        IFrame.MessageType.CONFIRMATION,
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
