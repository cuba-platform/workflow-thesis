/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 17:30
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.forms;

import com.haulmont.workflow.core.app.design.FormBuilder;
import org.dom4j.Element;
import org.json.JSONObject;

public class ResolutionFormBuilder extends FormBuilder {

    @Override
    public void writeFormEl(Element parentEl, JSONObject jsProperties) {
        Element el = parentEl.addElement("screen");
        el.addAttribute("id", "resolution.form");
        el.addAttribute("before", "true");

        addFormParam(el, "attachmentsVisible", Boolean.toString(jsProperties.optBoolean("attachmentsVisible")));
        addFormParam(el, "commentRequired", Boolean.toString(jsProperties.optBoolean("commentRequired")));
    }
}
