package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.entity.Attachment;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
@ManagedBean("workflow_AttachmentEntityListener")
public class AttachmentEntityListener implements BeforeInsertEntityListener<Attachment> {

    @Inject
    protected UserSessionSource userSessionSource;

    @Override
    public void onBeforeInsert(Attachment entity) {
        if (entity.getSubstitutedCreator() == null)
            entity.setSubstitutedCreator(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
    }
}
