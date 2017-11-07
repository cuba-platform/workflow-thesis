/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.activity

import com.haulmont.chile.core.model.utils.InstanceUtils
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.workflow.core.app.CardPropertyHandlerLoader
import com.haulmont.workflow.core.app.valuehandler.CardPropertyHandler
import com.haulmont.workflow.core.entity.Card
import com.haulmont.workflow.core.enums.AttributeType
import com.haulmont.workflow.core.enums.OperationsType
import com.haulmont.workflow.core.global.CardPropertyUtils
import org.jbpm.api.activity.ActivityExecution

public class ReadCardPropertyActivity extends CardPropertyActivity {

    String operationType

    CardPropertyHandler propertyValueObjectLoader

    final String YES_TRANSITION = "Yes";
    final String NO_TRANSITION = "No";

    public void execute(ActivityExecution execution) throws Exception {
        super.execute(execution)
        propertyValueObjectLoader = AppBeans.get(CardPropertyHandlerLoader.NAME, CardPropertyHandlerLoader.class).loadHandler(propertyClass, card, false);
        compareCardProperty(card, execution);
    }

    private void compareCardProperty(Card eventCard, ActivityExecution execution) {
        OperationsType operationsType = OperationsType.fromId(operationType);

        Object cardValue = InstanceUtils.getValueEx(eventCard, propertyPath);
        AttributeType attributeType = objectLoader.getAttributeType();
        checkConstraints(attributeType, operationsType, cardValue);

        String result;
        if (OperationsType.EMPTY == operationsType) {
            result = cardValue == null ? YES_TRANSITION : NO_TRANSITION;
        } else if (OperationsType.NOT_EMPTY == operationsType) {
            result = cardValue != null ? YES_TRANSITION : NO_TRANSITION;
        } else {
            Object targetValue = objectLoader.getValue(value);
            if (value != null && targetValue == null) {
                throw new RuntimeException("Unsupported value '" + value + "' for property '" + propertyPath + "'");
            }
            if (targetValue instanceof Double && cardValue instanceof BigDecimal) {
                targetValue = new BigDecimal((Double) targetValue)
            }
            boolean checkResult = CardPropertyUtils.compareValue(operationsType, cardValue, targetValue);

            if (checkResult) {
                result = YES_TRANSITION;
            } else {
                result = NO_TRANSITION;
            }
        }

        execution.take(result);

    }

    void checkConstraints(AttributeType attributeType, OperationsType operationsType, Object targetValue) {
        if (propertyPath == null) {
            throw new RuntimeException("Property path is null");
        }
        if (operationsType == null) {
            throw new RuntimeException("Unknown operations type: " + operationType);
        }
        if (!OperationsType.availableOps(attributeType).contains(operationsType)) {
            throw new RuntimeException("Unsupported operation '" + messages.getMessage(operationsType) + "' for entity attribute with type '" + messages.getMessage(attributeType) + "'")
        }
    }

}
