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

import org.jbpm.api.activity.ActivityBehaviour
import org.jbpm.api.activity.ActivityExecution
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.app.design.DesignDeployer
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import com.haulmont.cuba.core.global.ScriptingProvider

class Decision implements ActivityBehaviour {

  final String YES_TRANSITION = "yes";
  final String NO_TRANSITION = "no";

  String scriptName

  private Log log = LogFactory.getLog(Decision.class);

  void execute(ActivityExecution execution) {
    Card card = ActivityHelper.findCard(execution)

    Map<String, Object> params = new HashMap<String, Object>();
    params.put("card", card);
    params.put("activity", execution.getActivityName())

    String processKey = card.getProc().getJbpmProcessKey()
    String fileName = "process/" + processKey + "/" + DesignDeployer.SCRIPTS_DIR + "/" + scriptName

    log.debug("Running script " + fileName)
    Object result = ScriptingProvider.runGroovyScript(fileName, params)
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
