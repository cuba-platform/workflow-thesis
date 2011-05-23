/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.cuba.gui.UserSessionClient;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.actions.CreateAction;
import com.haulmont.cuba.gui.components.actions.EditAction;
import com.haulmont.cuba.gui.components.actions.RemoveAction;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardAttachment;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentActionsHelper;
import com.haulmont.workflow.web.ui.base.attachments.AttachmentCreator;

import java.util.Collections;
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
        createPopup.addAction(AttachmentActionsHelper.createMultiUploadAction(attachmentsTable, this, attachmentCreator));

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
    }

    public AttachmentCreator getAttachmentCreator() {
        return this.attachmentCreator;
    }
}
