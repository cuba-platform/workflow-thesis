/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

public class IsRoleAssignedModule extends Module {
    private String roleKey;

    @Override

    public void init(Context context) throws DesignCompilationException {
        super.init(context);

        JSONObject jsOptions = jsValue.optJSONObject("options");
        if (jsOptions != null) {
            String name = jsOptions.optString("name");
            if (!StringUtils.isBlank(name)) {
                this.name = WfUtils.encodeKey(name);
                this.caption = name;
            }
            this.roleKey = jsOptions.optString("roleKey");
            if (StringUtils.trimToNull(this.roleKey) == null)
                throw new DesignCompilationException(AppBeans.get(Messages.class).formatMessage(getClass(), "exception.noRole", caption));
        }
    }

    @Override
    public Element writeJpdlMainEl(Element parentEl) {
        Element el = parentEl.addElement("custom");
        el.addAttribute("name", name);
        Element property = el.addElement("property");
        property.addAttribute("name", "role");
        Element string = property.addElement("string");
        string.addAttribute("value", roleKey);
        el.addAttribute("class", "com.haulmont.workflow.core.activity.IsRoleAssignedDecider");
        return el;
    }
}
