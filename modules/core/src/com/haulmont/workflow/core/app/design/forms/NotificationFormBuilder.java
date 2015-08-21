/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.design.FormBuilder;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

public class NotificationFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) throws DesignCompilationException {
        Element el = parentEl.addElement("invoke");
        el.addAttribute("class", "com.haulmont.workflow.gui.app.base.NotificationForm");
        el.addAttribute("after", "true");
        String notification = jsProperties.optString("message");
        if (StringUtils.trimToNull(notification)==null){
            throw new DesignCompilationException(AppBeans.get(Messages.class).getMessage(getClass(), "exception.emptyNotification"));
        }
        addFormParam(el, "message", notification);

        return el;
    }
}
