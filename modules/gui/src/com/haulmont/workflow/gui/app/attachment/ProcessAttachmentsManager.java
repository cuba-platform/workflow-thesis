/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.attachment;

import com.google.common.base.Preconditions;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardAttachment;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 */

@Component(ProcessAttachmentsAPI.NAME)
public class ProcessAttachmentsManager implements ProcessAttachmentsAPI {

    @Override
    public List<Entity> copyAttachments(Collection<Assignment> assignments) {
        List<Entity> commitList = new ArrayList<>();
        if (!assignments.isEmpty()) {
            Assignment assignment = assignments.iterator().next();
            List<CardAttachment> attachmentList = assignment.getAttachments();
            if (assignments.size() > 1 && attachmentList != null) {
                for (Assignment item : assignments) {
                    if (item.getId().equals(assignment.getId())) {
                        continue;
                    }
                    Preconditions.checkNotNull(item, "Assignment is null");
                    List<CardAttachment> copyAttachmentList = new ArrayList<>();
                    for (CardAttachment attachment : attachmentList) {
                        CardAttachment cardAttachment = AppBeans.get(Metadata.class).create(CardAttachment.class);
                        cardAttachment.setAssignment(item);
                        cardAttachment.setCard(item.getCard().getFamilyTop());
                        cardAttachment.setFile(attachment.getFile());
                        cardAttachment.setName(attachment.getName());
                        cardAttachment.setAttachType(attachment.getAttachType());
                        cardAttachment.setComment(attachment.getComment());
                        copyAttachmentList.add(cardAttachment);
                        commitList.add(cardAttachment);
                    }
                    if (!copyAttachmentList.isEmpty())
                        item.setAttachments(copyAttachmentList);
                }
            }
        }
        return commitList;
    }
}
