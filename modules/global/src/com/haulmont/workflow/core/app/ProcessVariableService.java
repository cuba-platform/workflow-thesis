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
