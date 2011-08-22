/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.impl.DsListenerAdapter;
import com.haulmont.cuba.web.app.FileDownloadHelper;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentColumnGeneratorHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;
import com.haulmont.workflow.web.ui.base.attachments.NewVersionAction;

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
    protected boolean isStarted = false;

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
        createPopup.addAction(
                AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this,
                        attachmentCreator, WindowManager.OpenType.DIALOG)
        );


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
        attachmentsTable.addAction(new RemoveAction(attachmentsTable, false, "remove"));

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

        attachmentsTable.getDatasource().addListener(new DsListenerAdapter() {
            @Override
            public void stateChanged(Datasource ds, Datasource.State prevState, Datasource.State state) {
                if (state == Datasource.State.VALID && !isStarted) {
                    FileDownloadHelper.initGeneratedColumn(attachmentsTable, "file");
                    AttachmentColumnGeneratorHelper.addSizeGeneratedColumn(attachmentsTable);
                    isStarted = true;
                }
            }
        });
    }

    public AttachmentCreator getAttachmentCreator() {
        return this.attachmentCreator;
    }
}
