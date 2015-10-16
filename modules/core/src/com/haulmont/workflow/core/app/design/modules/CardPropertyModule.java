/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.workflow.core.app.CardPropertyHandlerLoader;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.app.valuehandler.CardPropertyHandler;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.global.CardPropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Element;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zaharchenko
 * @version $Id$
 */
public abstract class CardPropertyModule extends Module {

    protected JSONObject jsOptions;

    protected Set<String> transitionNames;

    protected String propertyPath;

    protected Boolean useExpression;

    protected String value;

    protected String cardClass;

    protected Class propertyPathClass;

    protected CardPropertyHandler objectLoader;

    public CardPropertyModule() {
    }

    @Override
    public void init(Module.Context context) throws DesignCompilationException {
        super.init(context);
        jsOptions = jsValue.optJSONObject("options");
        initOptions();
        propertyPathClass = getClassFromPropertyPath();
        CardPropertyHandlerLoader designManagerAPI = AppBeans.get(CardPropertyHandlerLoader.NAME);
        objectLoader = designManagerAPI.loadHandler(propertyPathClass, null, false);
        checkProperties();
        getAttributeTypeFromClass();
    }

    protected void initOptions() {
        JSONObject propertyPathValues = jsValue.optJSONObject("propertyPath");
        if (propertyPathValues == null) {
            propertyPath = jsValue.optString("propertyPath");
        } else {
            propertyPath = propertyPathValues.optString("systemPropertyValue");
        }
        value = jsValue.optString("value");
        useExpression = jsValue.optBoolean("useExpresssion", false);
        cardClass = jsValue.optString("cardClass");
    }

    protected void checkProperties() throws DesignCompilationException {
        if ((StringUtils.isEmpty(propertyPath) && !isVariableExists("propertyPath"))) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required 'property path' field is empty");
        }
        if (StringUtils.isEmpty(value) && !isVariableExists("value")) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required 'value' field is empty");
        }
        if (StringUtils.isEmpty(cardClass)) {
            throw new DesignCompilationException("Unable to compile module " + name + " : required 'cardClass' field is empty");
        }
        checkValue();
    }

    protected void checkValue() throws DesignCompilationException {
        if (isVariableExists("value") || useExpression)
            return;
        Object resultValue = objectLoader.getValue(value);
        if (resultValue == null) {
            throw new DesignCompilationException("Unsupported value '" + value + "' for property '" + propertyPath + "'");
        }
    }

    protected AttributeType getAttributeTypeFromClass() throws DesignCompilationException {
        AttributeType attributeType = objectLoader.getAttributeType();
        if (attributeType == null) {
            throw new DesignCompilationException("Unsupported class '" + propertyPathClass + "' for property path '" + propertyPath + "'");
        }
        return attributeType;
    }

    private Class getClassFromPropertyPath() throws DesignCompilationException {
        try {
            Metadata metadata = AppBeans.get(Metadata.NAME);
            MetaClass metaClass = metadata.getSession().getClass(Class.forName(cardClass));

            Class clazz = CardPropertyUtils.getClassByMetaProperty(metaClass, propertyPath);
            if (clazz == null) {
                throw new DesignCompilationException("Path '" + propertyPath + "' not found in class '" + metaClass.getName() + "'");
            }
            return clazz;
        } catch (ClassNotFoundException e) {
            throw new DesignCompilationException("Cant load metaclass " + cardClass, e);
        }
    }

    @Override
    public List<DesignProcessVariable> generateDesignProcessVariables() throws DesignCompilationException {
        super.generateDesignProcessVariables();

        DesignProcessVariable valueVariable = getVariableByPropertyName("value");
        if (valueVariable != null) {
            valueVariable.setPropertyName("value");
            if (useExpression) {
                valueVariable.setAttributeType(AttributeType.STRING);
            } else {
                valueVariable.setAttributeType(objectLoader.getAttributeType());
                valueVariable.setMetaClassName(objectLoader.getMetaClassName());
            }
        }

        return designProcessVariables;
    }

    @Override
    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        Element element = super.writeJpdlXml(parentEl);
        writeJpdlStringPropertyEl(element, "propertyPath", propertyPath);
        writeJpdlStringPropertyEl(element, "value", value);
        writeJpdlBooleanPropertyEl(element, "useExpression", useExpression);
        writeJpdlStringPropertyEl(element, "cardClass", cardClass);
        return element;
    }

    protected void setTransitionNames(String... names) {
        transitionNames = new HashSet<>();
        Collections.addAll(transitionNames, names);
    }
}