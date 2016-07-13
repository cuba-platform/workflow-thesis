/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.listeners;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.core.listener.BeforeInsertEntityListener;
import com.haulmont.workflow.core.entity.Attachment;

import org.springframework.stereotype.Component;
import javax.inject.Inject;

@Component("workflow_AttachmentEntityListener")
public class AttachmentEntityListener implements BeforeInsertEntityListener<Attachment> {

    @Inject
    protected UserSessionSource userSessionSource;

    @Override
    public void onBeforeInsert(Attachment entity, EntityManager entityManager) {
        if (entity.getSubstitutedCreator() == null)
            entity.setSubstitutedCreator(userSessionSource.getUserSession().getCurrentOrSubstitutedUser());
    }
}
