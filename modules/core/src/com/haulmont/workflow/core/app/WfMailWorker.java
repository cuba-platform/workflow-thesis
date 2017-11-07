/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;
import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@Component(WfMailWorker.NAME)
public class WfMailWorker {

    public static final String NAME = "workflow_MailWorker";

    private Log log = LogFactory.getLog(WfMailWorker.class);

    @Inject
    protected UserSessionSource userSessionSource;

    @Inject
    protected Scripting scripting;

    @Inject
    protected Resources resources;

    @Inject
    protected EmailerAPI emailer;

    public void sendCardMail(Card card, String comment, List<User> users, String script) {
        String subject;
        String body;

        if (card == null)
            return;
        if (users == null)
            return;

        for (User user : new LinkedList<>(users)) {
            if (StringUtils.trimToNull(user.getEmail()) != null) {
                log.debug("Card " + card.getDescription() + card.getLocState() + " send user " + user.getLogin() + " by email "
                        + user.getEmail() + " with comment " + comment);
                try {
                    Binding binding = new Binding();
                    binding.setVariable("card", card);
                    binding.setVariable("comment", comment);
                    binding.setVariable("user", userSessionSource.getUserSession().getUser());
                    binding.setVariable("users", users);
                    binding.setVariable("currentUser", user);
                    String scriptStr = resources.getResourceAsString(script);
                    scripting.evaluateGroovy(scriptStr, binding);
                    subject = binding.getVariable("subject").toString();
                    body = binding.getVariable("body").toString();

                } catch (Exception e) {
                    log.warn("Unable to get email subject and body, using defaults", e);
                    subject = String.format("Notification: %1$s - %2$s", card.getDescription(), card.getLocState());
                    body = String.format("Card %1$s has become %2$s \nComment: %3$s", card.getDescription(), card.getLocState(), comment);
                }
                try {
                    sendEmail(user, subject, body);
                } catch (EmailException ex) {
                    log.warn(ex);
                }
            }
        }
    }

    public void sendEmail(User user, String caption, String body, EmailAttachment... attachment) throws EmailException {
        EmailInfo info = new EmailInfo(user.getEmail(), caption, null, body, attachment);
        emailer.sendEmailAsync(info);
    }
}
