/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 17:40
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.workflow.core.app.design.FormBuilder;
import org.dom4j.Element;
import org.json.JSONObject;

public class TransitionFormBuilder extends FormBuilder {

    @Override
    public Element writeFormEl(Element parentEl, JSONObject jsProperties) {
        Element el = parentEl.addElement("screen");
        el.addAttribute("id", "transition.form");
        el.addAttribute("before", "true");

        addFormParam(el, "cardRolesVisible", Boolean.toString(jsProperties.optBoolean("cardRolesVisible")));
        addFormParam(el, "commentVisible", Boolean.toString(jsProperties.optBoolean("commentVisible")));
        addFormParam(el, "dueDateVisible", Boolean.toString(jsProperties.optBoolean("dueDateVisible")));
        addFormParam(el, "refusedOnlyVisible", Boolean.toString(jsProperties.optBoolean("refusedOnlyVisible")));
        addFormParam(el, "requiredRoles", jsProperties.optString("requiredRoles"));
        addFormParam(el, "hideAttachments", Boolean.toString(jsProperties.optBoolean("hideAttachments")));
        addFormParam(el, "requiredAttachmentTypes", jsProperties.optString("requiredAttachmentTypes"));
        addFormParam(el, "formHeight", jsProperties.optString("formHeight"));

        return el;
    }
}
