/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 17.01.11 15:32
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import org.dom4j.Element;
import org.json.JSONObject;

public abstract class FormBuilder {

    public abstract void writeFormEl(Element parentEl, JSONObject jsProperties);

    protected void addFormParam(Element el, String name, String value) {
        if (value == null)
            return;

        Element paramEl = el.addElement("param");
        paramEl.addAttribute("name", name);
        paramEl.addAttribute("value", value);
    }
}
