/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.EmailAttachment;
import com.haulmont.cuba.core.global.EmailException;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;

import java.util.List;

public interface MailService {

    public static String NAME = "workflow_MailService";

    /**
     * Sends email, performed by a specified script to given users.
     *
     * @param card    card in a workflow process
     * @param comment comment to a card
     * @param users   email recipients
     * @param script  script to perform a email
     */
    void sendCardMail(Card card, String comment, List<User> users, String script);

    /**
     * Sends an email to a given user.
     *
     * @param user       email recipient
     * @param caption    email caption
     * @param body       email body
     * @param attachment email attachments
     * @throws EmailException
     */
    void sendEmail(User user, String caption, String body, EmailAttachment... attachment) throws EmailException;

}
