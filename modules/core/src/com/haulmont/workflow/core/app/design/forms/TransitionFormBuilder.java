/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
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
        addFormParam(el, "visibleRoles", jsProperties.optString("visibleRoles"));
        addFormParam(el, "hideAttachments", Boolean.toString(jsProperties.optBoolean("hideAttachments")));
        addFormParam(el, "requiredAttachmentTypes", jsProperties.optString("requiredAttachmentTypes"));
        addFormParam(el, "formHeight", jsProperties.optString("formHeight"));
        addFormParam(el, "commentRequired", jsProperties.optString("commentRequired"));

        return el;
    }
}
