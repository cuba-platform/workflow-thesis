/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 01.02.11 17:08
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

public class ParallelAssignmentModule extends AssignmentModule {

    public ParallelAssignmentModule() {
        activityClassName = "workflow.activity.ParallelAssigner";
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        if (jsOptions != null) {
            String successTransition = jsOptions.optString("successTransition");
            if (!StringUtils.isBlank(successTransition)) {
                writeJpdlStringPropertyEl(element, "successTransition", WfUtils.encodeKey(successTransition));
            }

            boolean refusedOnly = jsOptions.optBoolean("refusedOnly");
            if (refusedOnly) {
                writeJpdlBooleanPropertyEl(element, "refusedOnly", true);
            }
        }
        return element;
    }
}
