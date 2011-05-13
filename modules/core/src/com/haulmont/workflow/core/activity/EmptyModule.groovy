/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.activity

import org.jbpm.api.activity.ExternalActivityBehaviour
import org.jbpm.api.activity.ActivityExecution

/**
 * 
 * <p>$Id$</p>
 *
 * @author devyatkin
 */

public class EmptyModule implements ExternalActivityBehaviour {
  void signal(ActivityExecution execution, String signalName, Map<String, ?> parameters) {
    execution.take(signalName);
  }

  void execute(ActivityExecution execution) {
      execution.take("out");
  }
}
