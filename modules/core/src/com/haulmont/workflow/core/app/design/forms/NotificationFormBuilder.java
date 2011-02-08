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

import com.haulmont.workflow.core.app.design.FormBuilder;
import org.dom4j.Element;
import org.json.JSONObject;

public class NotificationFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) {
        Element el = parentEl.addElement("invoke");
        el.addAttribute("class", "com.haulmont.workflow.web.ui.base.NotificationForm");
        el.addAttribute("after", "true");

        addFormParam(el, "message", jsProperties.optString("message"));

        return el;
    }
}
