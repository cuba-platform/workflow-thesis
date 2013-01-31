/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.text.MessageFormat;

/**
 * @author subbotin
 * @version $Id$
 */
public class SubProcModule extends Module {

    protected String subProcCode;

    public SubProcModule() {
        activityClassName = "com.haulmont.workflow.core.activity.SubProc";
    }

    @Override
    public void init(Context context) throws DesignCompilationException {
        super.init(context);
        subProcCode = jsValue.optString("subProcCode");
        if (StringUtils.isBlank(subProcCode)) {
            Messages messages = AppBeans.get(Messages.class);
            throw new DesignCompilationException(messages.formatMessage(SubProcModule.class, "exception.noProc", caption));
        }
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        writeJpdlStringPropertyEl(element, "subProcCode", subProcCode);
        writeJpdlStringPropertyEl(element, "card", "subProcCard");
        return element;
    }
}
