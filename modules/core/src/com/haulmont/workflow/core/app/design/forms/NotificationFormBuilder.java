/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 15:29
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.workflow.core.app.design.FormBuilder;

import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

public class NotificationFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException {
        Element el = parentEl.addElement("invoke");
        el.addAttribute("class", "com.haulmont.workflow.web.ui.base.NotificationForm");
        el.addAttribute("after", "true");
        String notification = jsProperties.optString("message");
        if (StringUtils.trimToNull(notification)==null){
            throw new DesignCompilationException(MessageProvider.getMessage(getClass(), "exception.emptyNotification"));
        }
        addFormParam(el, "message", notification);

        return el;
    }
}
