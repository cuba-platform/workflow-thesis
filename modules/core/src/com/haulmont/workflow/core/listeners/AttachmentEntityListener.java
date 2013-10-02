package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.entity.Attachment;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class AttachmentEntityListener implements BeforeInsertEntityListener<Attachment> {

    protected UserSessionSource userSessionSource = AppBeans.get(UserSessionSource.NAME);

    @Override
    public void onBeforeInsert(Attachment entity) {
        entity.setUser(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
    }
}
