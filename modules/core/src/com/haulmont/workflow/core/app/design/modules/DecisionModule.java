/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 04.02.11 12:48
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

public class DecisionModule extends Module {

    protected JSONObject jsOptions;

    protected String scriptFileName;

    public DecisionModule() {
        activityClassName = "com.haulmont.workflow.core.activity.Decision";
    }

    @Override
    public void init(Context context) throws DesignCompilationException {
        super.init(context);
        jsOptions = jsValue.optJSONObject("options");

        String name = jsOptions.optString("name");
        if (!StringUtils.isBlank(name))
            this.name = WfUtils.encodeKey(name);

        String script = null;
        this.scriptFileName = null;
        if (jsOptions != null) {
            script = jsOptions.optString("script");
            this.scriptFileName = scriptNamesMap.get(script);
        }
        if (this.scriptFileName == null)
            throw new DesignCompilationException("Unable to compile DecisionModule " + caption
                    + ": script '" + script + "' not found");
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        writeJpdlStringPropertyEl(element, "scriptName", scriptFileName);
        return element;
    }
}
