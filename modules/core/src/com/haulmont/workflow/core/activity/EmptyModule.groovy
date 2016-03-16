/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import org.jbpm.api.activity.ExternalActivityBehaviour
import org.jbpm.api.activity.ActivityExecution

/**
 * 
 *
 */

public class EmptyModule implements ExternalActivityBehaviour {
  void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    execution.take(signalName);
  }

  void execute(ActivityExecution execution) {
      execution.take("out");
  }
}
