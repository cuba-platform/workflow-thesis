/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.List;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DecisionModule extends Module {

    protected JSONObject jsOptions;

    protected String scriptFileName;

    protected String scriptPath;

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

        Messages messages = AppBeans.get(Messages.class);

        if (jsOptions != null) {
            script = jsOptions.optString("script");

            if (StringUtils.trimToNull(script) == null) {
                throw new DesignCompilationException(messages.formatMessage(getClass(),
                        "exception.decisionScriptNotDefined", StringEscapeUtils.escapeHtml(caption)));
            }

            for (DesignScript designScript : context.getDesign().getScripts()) {
                if (ObjectUtils.equals(designScript.getName(), script)
                        && StringUtils.isNotEmpty(designScript.getContent())
                        && designScript.getContent().trim().endsWith(".groovy")) {
                    this.scriptPath = designScript.getContent().trim();
                }
            }
            if (scriptPath == null) {
                this.scriptFileName = scriptNamesMap.get(script);
            }
        }

        this.scriptFileName = scriptNamesMap.get(script);
        if (this.scriptFileName == null)
            throw new DesignCompilationException(messages.formatMessage(getClass(),
                    "exception.decisionScriptNotFound", StringEscapeUtils.escapeHtml(caption), script));
    }

    @Override
    public List<DesignProcessVariable> generateDesignProcessVariables() throws DesignCompilationException {
        super.generateDesignProcessVariables();
        DesignProcessVariable variable = getVariableByPropertyName("script");
        if (variable != null) {
            variable.setPropertyName("scriptName");
        }
        return designProcessVariables;
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        if (scriptFileName != null) {
            writeJpdlStringPropertyEl(element, "scriptName", scriptFileName);
        } else if (scriptPath != null) {
            writeJpdlStringPropertyEl(element, "scriptPath", scriptPath);
        }
        return element;
    }
}