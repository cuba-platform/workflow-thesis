/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.AbstractProcessVariable;

public interface ProcessVariableService {

    String NAME = "workflow_ProcessVariableService";

    String getStringValue(Object value);

    Object getValue(AbstractProcessVariable designProcessVariable);

    String getLocalizedValue(AbstractProcessVariable designProcessVariable);

}
