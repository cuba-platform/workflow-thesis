/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;

/**
 * @author chekashkin
 * @version $Id$
 */
public interface AssignmentLocalizationTools {

    String NAME = "workflow_AssignmentLocalizationTools";

    String getLocalizedAttribute(Assignment assignment, String value);

    String getLocOutcomeResult(Assignment assignment);

    String getLocOutcome(Assignment assignment);

}
