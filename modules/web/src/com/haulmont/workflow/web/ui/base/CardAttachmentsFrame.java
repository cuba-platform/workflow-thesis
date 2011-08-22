/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DatasourceImpl;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.cuba.web.gui.WebWindow;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.web.ui.base.attachments.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>$Id$</p>
 *
 * @author gorbunkov
 */
public class CardAttachmentsFrame extends AbstractFrame {

    protected Datasource<Card> cardDs;
    protected Table attachmentsTable;
    protected AttachmentCreator attachmentCreator;

    private boolean generatedColumnInited = false;

    public CardAttachmentsFrame(IFrame frame) {
        super(frame);
    }

    public void init() {
        cardDs = getDsContext().get("cardDs");
        attachmentsTable = getComponent("attachmentsTable");


        attachmentCreator = new AttachmentCreator() {
            public Attachment createObject() {
                CardAttachment attachment = new CardAttachment();
                Card card = cardDs.getItem();
                attachment.setCard(card);
                attachment.setCreateTs(TimeProvider.currentTimestamp());
                attachment.setCreatedBy(UserSessionClient.getUserSession().getCurrentOrSubstitutedUser().getLoginLowerCase());
                return attachment;
            }
        };

        PopupButton createPopup = getComponent("createAttachBtn");
        Action multiUploadAction = AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this,
                attachmentCreator, WindowManager.OpenType.DIALOG);

        createPopup.addAction(new CommitCardAction("actions.MultiUpload", multiUploadAction));

        NewVersionAction newVersionAction = new NewVersionAction(attachmentsTable, WindowManager.OpenType.DIALOG) {
            @Override
            protected Map<String, Object> getInitialValues() {
                Map<String, Object> initialValues = new HashMap();
                initialValues.put("card", cardDs.getItem());
                initialValues.put("file", new FileDescriptor());
                return initialValues;
            }
        };

        createPopup.addAction(newVersionAction);

        attachmentsTable.addAction(newVersionAction);
        attachmentsTable.addAction(new EditAction(attachmentsTable, WindowManager.OpenType.DIALOG, "edit"));
        attachmentsTable.addAction(new RemoveAttachmentAction(attachmentsTable, true, "remove"));

        Button copyAttachBtn = getComponent("copyAttach");
        copyAttachBtn.setAction(AttachmentActionsHelper.createCopyAction(attachmentsTable));
        copyAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Copy"));

        Button pasteAttachBtn = getComponent("pasteAttach");
        pasteAttachBtn.setAction(
                AttachmentActionsHelper.createPasteAction(attachmentsTable, attachmentCreator));
        pasteAttachBtn.setCaption(MessageProvider.getMessage(getClass(), "actions.Paste"));

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

        attachmentsTable.setStyleProvider(new Table.StyleProvider() {
            public String getStyleName(Entity item, Object property) {
                return ((Attachment) item).getVersionOf() != null ? "grey" : null;
            }

            public String getItemIcon(Entity item) {
                return null;
            }
        });

        CollectionDatasource.Sortable.SortInfo sortInfo = new CollectionDatasource.Sortable.SortInfo();
        sortInfo.setOrder(CollectionDatasource.Sortable.Order.DESC);
        sortInfo.setPropertyPath(attachmentDs.getMetaClass().getPropertyPath("createTs"));
        ((CollectionDatasourceImpl) attachmentDs).sort(new CollectionDatasource.Sortable.SortInfo[]{sortInfo});

        attachmentDs.refresh();

    }


    public AttachmentCreator getAttachmentCreator() {
        return this.attachmentCreator;
    }

    protected class CommitCardAction extends AbstractAction {

        private Action afterPerformAction;

        protected CommitCardAction(String id, Action afterPerformAction) {
            super(id);
            this.afterPerformAction = afterPerformAction;
        }

        public void actionPerform(Component component) {
            if (PersistenceHelper.isNew(cardDs.getItem())) {
                showOptionDialog(
                        getMessage("cardAttachmentFrame.dialogHeader"),
                        getMessage("cardAttachmentFrame.dialogMessage"),
                        IFrame.MessageType.CONFIRMATION,
                        new Action[]{
                                new DialogAction(DialogAction.Type.YES) {
                                    @Override
                                    public void actionPerform(Component component) {
                                        ((DatasourceImpl) getDsContext().get("cardDs")).setModified(true);
                                        boolean isCommited = true;
                                        if (getFrame() instanceof WebWindow.Editor)
                                            isCommited = ((AbstractEditor) ((WebWindow.Editor) getFrame()).getWrapper()).commit(true);
                                        else
                                            getFrame().getDsContext().commit();

                                        if (isCommited)
                                            afterPerformAction.actionPerform(null);
                                    }
                                },
                                new DialogAction(DialogAction.Type.NO)
                        });
            } else {
                afterPerformAction.actionPerform(null);
            }
        }
    }
}
