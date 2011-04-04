/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Devyatkin
 * Created: 01.04.2011 12:52:29
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
        el.addAttribute("class", "workflow.activity.IsRoleAssignedDecider");
        return el;
    }
}
