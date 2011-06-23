/*
* Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
* Haulmont Technology proprietary and confidential.
* Use is subject to license terms.

* Author: Konstantin Devyatkin
* Created: 28.02.11 11:05
*
* $Id$
*/
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;


public class JoinModule extends Module {
    @Override
    public void init(Module.Context context) throws DesignCompilationException {
        super.init(context);

        JSONObject jsOptions = jsValue.optJSONObject("options");
        if (jsOptions != null) {
            String name = jsOptions.optString("name");
            if (!StringUtils.isBlank(name)) {
                this.name = WfUtils.encodeKey(name);
                this.caption = name;
            }
        }
    }

    @Override
    public Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("join");
        el.addAttribute("name", name);
        Element onElement = el.addElement("on");
        onElement.addAttribute("event","end");
        Element eventListener = onElement.addElement("event-listener");
        eventListener.addAttribute("class","workflow.activity.CardStateListener");
        return el;
    }

}
