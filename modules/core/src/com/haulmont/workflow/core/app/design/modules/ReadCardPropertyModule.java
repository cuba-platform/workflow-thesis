/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.enums.OperationsType;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;

import java.util.Arrays;
import java.util.List;

/**
 */
public class ReadCardPropertyModule extends CardPropertyModule {

    protected String operationType;

    public ReadCardPropertyModule() {
        activityClassName = "com.haulmont.workflow.core.activity.ReadCardPropertyActivity";
        setTransitionNames("Yes", "No");
    }

    @Override
    protected void initOptions() {
        super.initOptions();
        operationType = jsValue.optString("operationType");
    }

    @Override
    protected void checkProperties() throws DesignCompilationException {
        if (StringUtils.isEmpty(propertyPath) && !isVariableExists("propertyPath")) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required field 'property path' is empty");
        }
        if (StringUtils.isEmpty(cardClass)) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required field 'cardClass' is empty");
        }
        if (StringUtils.isEmpty(operationType) && !isVariableExists("operationType")) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required field 'operation' is empty");
        }
        if (StringUtils.isEmpty(value) && !isVariableExists("value") && !Arrays.asList(OperationsType.EMPTY, OperationsType.NOT_EMPTY).contains(OperationsType.fromId
                (operationType))) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required field 'value' is empty");
        }
        checkValue();
    }

    @Override
    public void init(Module.Context context) throws DesignCompilationException {
        super.init(context);
        AttributeType attributeType = getAttributeTypeFromClass();
        if (StringUtils.isNotEmpty(propertyPath) && !isVariableExists("propertyPath")) {
            if (!isVariableExists("operationType")) {
                OperationsType opType = OperationsType.fromId(operationType);

                if (!OperationsType.availableOps(attributeType).contains(opType)) {
                    Messages messages = AppBeans.get(Messages.NAME);
                    throw new DesignCompilationException("Unsupported operation '" + messages.getMessage(opType) + "' for property path with type '" + messages
                            .getMessage(attributeType) + "'");
                }
            }

        }
    }

    @Override
    public List<DesignProcessVariable> generateDesignProcessVariables() throws DesignCompilationException {
        super.generateDesignProcessVariables();

        DesignProcessVariable operationTypeVariable = getVariableByPropertyName("operationType");
        if (operationTypeVariable != null) {
            operationTypeVariable.setPropertyName("operationType");
            operationTypeVariable.setAttributeType(AttributeType.ENUM);
            operationTypeVariable.setMetaClassName(OperationsType.class.getName());
        }

        return designProcessVariables;
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        writeJpdlStringPropertyEl(element, "operationType", operationType);
        return element;
    }
}