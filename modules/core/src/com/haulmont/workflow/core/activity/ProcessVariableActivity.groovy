/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.app.ProcessVariableAPI
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.entity.CardVariable
import com.haulmont.workflow.core.global.WfConstants
import org.apache.commons.lang.StringUtils
import org.jbpm.api.activity.ActivityBehaviour
import org.jbpm.api.activity.ActivityExecution
import org.jbpm.pvm.internal.util.ReflectUtil

import java.lang.reflect.Method

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
public class ProcessVariableActivity implements ActivityBehaviour {

    private ProcessVariableAPI processVariableAPI = AppBeans.get(ProcessVariableAPI.NAME);

    private Map<String, CardVariable> cardVariables;

    private String activityName;

    void execute(ActivityExecution execution) throws Exception {
        activityName = execution.getActivityName();
        Card card = ActivityHelper.findCard(execution);
        initCardVariables(card);
        injectCardVariables();
    }

    protected Object getVariable(String key) {
        CardVariable variable = cardVariables.get(key);
        return variable == null ? null : processVariableAPI.getValue(variable);
    }

    protected void setVariable(String key, Object value) {
        CardVariable variable = cardVariables.get(key);
        if (variable == null) {
            throw new IllegalStateException(String.format("Unknown variable with alias: %s", key));
        }
        variable.setValue(processVariableAPI.getStringValue(value));
    }

    protected void injectCardVariables() {
        for (CardVariable cardVariable : cardVariables.values()) {
            injectValue(WfConstants.VARIABLE_PREFIX + cardVariable.getAlias(), cardVariable);
            int moduleIndex = ActivityHelper.searchInStringWithSplit(activityName,
                    cardVariable.getModuleName(), WfConstants.CARD_VARIABLES_SEPARATOR)
            if (StringUtils.isNotBlank(cardVariable.getModuleName())
                    && StringUtils.isNotBlank(cardVariable.getPropertyName())
                    && moduleIndex >= 0) {
                String[] properties = cardVariable.getPropertyName().split(WfConstants.CARD_VARIABLES_SEPARATOR);
                String property = properties[moduleIndex];
                injectValue(property, cardVariable);
            }
        }
    }

    private void injectValue(String property, CardVariable cardVariable) {
        String setterName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1)
        Object value = processVariableAPI.getValue(cardVariable);
        Method method = null;
        Class<?> clazz = this.getClass();
        Object[] args = new Object[1];
        args[0] = value
        method = ReflectUtil.findMethod(clazz, setterName, null, args);
        if (method == null) {
            args[0] = cardVariable.getValue();
            method = ReflectUtil.findMethod(clazz, setterName, null, args);
        }
        if (method != null) {
            ReflectUtil.invoke(method, this, args);
        }
    }

    protected void initCardVariables(Card card) {
        cardVariables = new HashMap<String, CardVariable>()
        for (CardVariable cardVariable : card.getCardVariables()) {
            cardVariables.put(cardVariable.getAlias(), cardVariable);
        }
    }
}
