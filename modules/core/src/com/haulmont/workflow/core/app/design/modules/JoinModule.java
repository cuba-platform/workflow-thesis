/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

import java.text.MessageFormat;


public class JoinModule extends Module {

    protected String role;

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
            this.role = StringUtils.trimToNull(jsOptions.optString("role"));
        }
    }

    @Override
    public Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("join");
        el.addAttribute("name", name);
        if (role != null)
            el.addAttribute("multiplicity", MessageFormat.format("#'{'wf:getUserCntByProcRole(execution, \"{0}\")'}'", role));
        Element onElement = el.addElement("on");
        onElement.addAttribute("event", "end");
        Element eventListener = onElement.addElement("event-listener");
        eventListener.addAttribute("class", "com.haulmont.workflow.core.activity.CardStateListener");
        return el;
    }

}
