/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.AbstractProcessVariable;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardVariable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
public interface ProcessVariableAPI {

    String NAME = "workflow_ProcessVariableManager";

    String getStringValue(Object value);

    Object getValue(AbstractProcessVariable designProcessVariable);

    String getLocalizedValue(AbstractProcessVariable designProcessVariable);

    Map<String, CardVariable> getVariablesForCard(Card card);

    void createVariablesForCard(Card card);

    Map<String, AbstractProcessVariable> collectVariablesForCard(Card card);

    List<String> checkVariables(Collection<AbstractProcessVariable> variablesForCard);
}
