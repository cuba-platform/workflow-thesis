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

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.app.EmailerAPI;
import com.haulmont.cuba.core.global.EmailException;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;
import groovy.lang.Binding;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.LinkedList;
import java.util.List;

@Service(MailService.NAME)
public class MailServiceBean implements MailService  {

    protected Log log = LogFactory.getLog(MailServiceBean.class);

    @Inject
    private UserSessionSource userSessionSource;

    public void sendCardMail(Card card, String comment, List<User> users, String script) {
        String subject;
        String body;

        if(card == null)
           return;
        if(users == null)
           return;

        for(User user: new LinkedList<User>(users)){
            if(StringUtils.trimToNull(user.getEmail()) != null){
                log.debug("Card " + card.getDescription()+card.getLocState()+" send user "+user.getLogin()+ " by email "
                        +user.getEmail()+" with comment "+comment);
                try {
                    Binding binding = new Binding();
                    binding.setVariable("card", card);
                    binding.setVariable("comment", comment);
                    binding.setVariable("user", userSessionSource.getUserSession().getUser());
                    binding.setVariable("users", users);
                    binding.setVariable("currentUser", user);
                    ScriptingProvider.runGroovyScript(script, binding);
                    subject = binding.getVariable("subject").toString();
                    body = binding.getVariable("body").toString();

                } catch (Exception e) {
                    log.warn("Unable to get email subject and body, using defaults", e);
                    subject = String.format("Notification: %1$s - %2$s", card.getDescription(), card.getLocState());
                    body = String.format("Card %1$s has become %2$s \nComment: %3$s", card.getDescription(), card.getLocState(), comment);
                }
                Mailer mailer = new Mailer(user.getEmail(), subject, body);
                Thread t = new Thread(mailer);
                t.start();
            }
        }
    }
    private class Mailer implements Runnable{
        private String email;
        private String subject;
        private String body;
        
        public Mailer(String email, String subject, String body) {
            this.email = email;
            this.subject = subject;
            this.body = body;
        }

        public void run(){
            try{
                EmailerAPI emailer = Locator.lookup(EmailerAPI.NAME);
                emailer.sendEmail(email, subject, body);
            }catch(EmailException ex){
                log.warn("Error send mail", ex);
            }
        }

    }
}
