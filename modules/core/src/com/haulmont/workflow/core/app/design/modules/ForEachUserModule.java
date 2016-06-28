/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

import java.text.MessageFormat;

public class ForEachUserModule extends Module {

    protected String role;
    protected JSONObject jsOptions;

    @Override
    public void init(Context context) throws DesignCompilationException {
        super.init(context);
        jsOptions = jsValue.optJSONObject("options");
        role = jsOptions.optString("role");
        if (StringUtils.trimToNull(role) == null) {
            Messages messages = AppBeans.get(Messages.class);
            throw new DesignCompilationException(messages.formatMessage(ForEachUserModule.class, "exception.noRole", caption));
        }
    }

    @Override
    public Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("foreach");
        el.addAttribute("name", getName());
        el.addAttribute("var", "iteratedAssigner");
        el.addAttribute("in", MessageFormat.format("#'{'wf:getUsersByProcRole(execution, \"{0}\")'}'", role));
        return el;
    }
}
