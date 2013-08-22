/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Valery Novikov
 * Created: 30.06.2010 11:50:55
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.global.EmailAttachment;
import com.haulmont.cuba.core.global.EmailException;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service(MailService.NAME)
public class MailServiceBean implements MailService {

    @Inject
    protected WfMailWorker wfMailWorker;

    @Override
    public void sendCardMail(Card card, String comment, List<User> users, String script) {
        wfMailWorker.sendCardMail(card, comment, users, script);
    }

    @Override
    public void sendEmail(User user, String caption, String body, EmailAttachment... attachment) throws EmailException {
        wfMailWorker.sendEmail(user, caption, body, attachment);
    }
}