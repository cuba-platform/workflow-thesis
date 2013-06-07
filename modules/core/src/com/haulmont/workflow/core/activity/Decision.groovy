/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 04.02.11 12:53
 *
 * $Id$
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.Resources
import com.haulmont.cuba.core.global.Scripting
import com.haulmont.cuba.core.global.ScriptingProvider
import com.haulmont.workflow.core.app.design.DesignDeployer
import com.haulmont.workflow.core.entity.Card
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.jbpm.api.activity.ActivityExecution

class Decision extends ProcessVariableActivity {

    final String YES_TRANSITION = "yes";
    final String NO_TRANSITION = "no";

    String scriptName

    private Log log = LogFactory.getLog(Decision.class);

    void execute(ActivityExecution execution) {
        super.execute(execution);
        Card card = ActivityHelper.findCard(execution)

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("card", card);
        params.put("activity", execution.getActivityName())

        String processKey = card.getProc().getJbpmProcessKey()
        String fileName = "process/" + processKey + "/" + DesignDeployer.SCRIPTS_DIR + "/" + scriptName

        log.debug("Running script " + fileName)
        Scripting scripting = AppBeans.get(Scripting.class);
        Resources resources = AppBeans.get(Resources.class);
        Object result = scripting.evaluateGroovy(resources.getResourceAsString(fileName), params)
        if (result instanceof Boolean) {
            if (result)
                execution.take(YES_TRANSITION)
            else
                execution.take(NO_TRANSITION)
        } else {
            throw new RuntimeException("The script must return a boolean value");
        }
    }
}
