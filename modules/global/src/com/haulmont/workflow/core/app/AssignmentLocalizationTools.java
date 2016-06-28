/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;

public interface AssignmentLocalizationTools {

    String NAME = "workflow_AssignmentLocalizationTools";

    String getLocalizedAttribute(Assignment assignment, String value);

    String getLocOutcomeResult(Assignment assignment);

    String getLocOutcome(Assignment assignment);

}
