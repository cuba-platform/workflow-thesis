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

import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.Card;

import java.util.List;

public interface MailService {

    public static String NAME = "workflow_MailService";

    void sendCardMail(Card card, String comment, List<User> users, String script);

}
