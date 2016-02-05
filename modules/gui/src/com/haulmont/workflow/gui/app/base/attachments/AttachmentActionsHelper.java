/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.app.base.attachments;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.workflow.core.entity.Attachment;
import com.haulmont.workflow.gui.app.tools.AttachmentActionTools;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Create {@link Attachment} actions and buttons for attachments table
 * @deprecated use {@link com.haulmont.workflow.gui.app.tools.AttachmentActionTools} via DI
 * or <code>AppBeans.get(AttachmentActionTools.class)</code>
 */
@Deprecated()
public class AttachmentActionsHelper {

    public static final String COPY_ACTION_ID = "actions.Copy";
    public static final String PASTE_ACTION_ID = "actions.Paste";
    public static final String LOAD_ACTION_ID = "actions.Load";

    private AttachmentActionsHelper() {
    }

    /**
     * Create copy attachment action for table
     *
     * @param attachmentsTable Table with attachments
     * @return Action
     */
    public static Action createCopyAction(Table attachmentsTable) {
        return AppBeans.get(AttachmentActionTools.class).createCopyAction(attachmentsTable);
    }

    /**
     * Create paste attachment action for table
     *
     * @param attachmentsTable Table with attachments
     * @param creator          Custom method for set object properties
     * @return Action
     */
    public static Action createPasteAction(Table attachmentsTable, final AttachmentCreator creator) {
        return AppBeans.get(AttachmentActionTools.class).createPasteAction(attachmentsTable, creator);
    }

    public static Action createPasteAction(Table attachmentsTable, final AttachmentCreator creator, @Nullable Map<String,Object> params) {
        return AppBeans.get(AttachmentActionTools.class).createPasteAction(attachmentsTable, creator, params);
    }

    /**
     * Create load attachment context menu for attaghments table
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     */
    public static void createLoadAction(Table attachmentsTable, IFrame window) {
        AppBeans.get(AttachmentActionTools.class).createLoadAction(attachmentsTable, window);
    }

    /**
     * Create action for multiupload attachments
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     * @param creator          Custom method for set object properties
     * @return Multifile upload action
     */
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator) {
        return AppBeans.get(AttachmentActionTools.class).createMultiUploadAction(attachmentsTable, window, creator);
    }

    /**
     * Create action for multiupload attachments
     *
     * @param attachmentsTable Table with attachments
     * @param window           Window
     * @param creator          Custom method for set object properties
     * @param openType         Window open type
     * @param params           Dialog params
     * @return Multifile upload action
     */
    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, final AttachmentCreator creator,
                                                 final WindowManager.OpenType openType, final Map<String, Object> params) {
        return AppBeans.get(AttachmentActionTools.class).createMultiUploadAction(attachmentsTable, window, creator, openType, params);
    }

    public static Action createMultiUploadAction(Table attachmentsTable, IFrame window, AttachmentCreator creator,
                                                 final WindowManager.OpenType openType) {
        return AppBeans.get(AttachmentActionTools.class).createMultiUploadAction(attachmentsTable, window, creator, openType);
    }

    /**
     * Create single click upload button in {@link ButtonsPanel}
     *
     * @param attachmentsTable table
     * @param creator          Attachment creator
     * @param uploadScreenId   Attachment editor screen
     * @param windowParams     Additional params
     * @param openType         Screen open type
     * @return FileUploadField button
     */
    public static FileUploadField createFastUploadButton(final Table attachmentsTable,
                                                         final AttachmentCreator creator,
                                                         final String uploadScreenId,
                                                         @Nullable final Map<String, Object> windowParams,
                                                         final WindowManager.OpenType openType) {

        return AppBeans.get(AttachmentActionTools.class).createFastUploadButton(attachmentsTable, creator, uploadScreenId, windowParams, openType);
    }
}