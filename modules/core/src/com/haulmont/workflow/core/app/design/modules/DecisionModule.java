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

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.List;

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
        if (!StringUtils.isBlank(name)) {
            this.name = WfUtils.encodeKey(name);
            caption = name;
        }

        String script = null;
        this.scriptFileName = null;
        script = jsOptions.optString("script");

        Messages messages = AppBeans.get(Messages.class);

        if (StringUtils.trimToNull(script) == null) {
            throw new DesignCompilationException(messages.formatMessage(getClass(), "exception.decisionScriptNotDefined", caption));
        }
        this.scriptFileName = scriptNamesMap.get(script);
        if (this.scriptFileName == null)
            throw new DesignCompilationException(messages.formatMessage(getClass(), "exception.decisionScriptNotFound", caption, script));
    }

    @Override
    public List<DesignProcessVariable> getDesignProcessVariables() throws DesignCompilationException {
        super.getDesignProcessVariables();
        DesignProcessVariable variable = getVariableByPropertyName("script");
        if (variable != null) {
            variable.setPropertyName("scriptName");
        }
        return designProcessVariables;
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        if (!StringUtils.isEmpty(scriptFileName))
            writeJpdlStringPropertyEl(element, "scriptName", scriptFileName);
        return element;
    }
}
