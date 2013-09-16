/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.workflow.core.entity.*;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.annotation.ManagedBean;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;

/**
 *
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
@ManagedBean(ProcessVariableAPI.NAME)
public class ProcessVariableManager implements ProcessVariableAPI {

    private Log log = LogFactory.getLog(getClass());

    public String getStringValue(Object value) {
        String stringValue = null;
        if (value != null)
            if (value instanceof Integer)
                stringValue = Datatypes.get(Integer.class).format((Integer) value);
            else if (value instanceof BigDecimal)
                stringValue = Datatypes.get(BigDecimal.class).format((BigDecimal) value);
            else if (value instanceof Double)
                stringValue = Datatypes.get(Double.class).format((Double) value);
            else if (value instanceof Date)
                stringValue = Long.valueOf(((Date) value).getTime()).toString();
            else if (value instanceof Entity)
                stringValue = ((Entity) value).getId().toString();
            else if (value instanceof UUID)
                stringValue = value.toString();
            else if (value instanceof Enum)
                stringValue = ((EnumClass) value).getId().toString();
            else stringValue = value.toString();
        return stringValue;
    }

    public Object getValue(AbstractProcessVariable designProcessVariable) {

        Object value = null;
        String stringValue = designProcessVariable.getValue();
        if (StringUtils.isBlank(stringValue)) return stringValue;
        if (designProcessVariable.getAttributeType() == null) return stringValue;
        try {
            switch (designProcessVariable.getAttributeType()) {
                case INTEGER:
                    value = Datatypes.get(Integer.class).parse(stringValue);
                    break;

                case DOUBLE:
                    value = Datatypes.get(Double.class).parse(stringValue);
                    break;

                case STRING:
                    value = stringValue;
                    break;

                case DATE:
                    value = new Date(Long.parseLong(stringValue));
                    break;

                case DATE_TIME:
                    value = new Date(Long.parseLong(stringValue));
                    break;

                case BOOLEAN:
                    value = Datatypes.get(Boolean.class).parse(stringValue);
                    break;

                case ENTITY:
                    Class entityClass = AppBeans.get(Metadata.class).getSession()
                            .getClass(designProcessVariable.getMetaClassName()).getJavaClass();
                    EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
                    Entity entity = em.find(entityClass, UUID.fromString(stringValue));
                    if (entity != null) {
                        InstanceUtils.getInstanceName(entity);
                        value = entity;
                    }
                    break;

                case ENUM:
                    try {
                        Class enumClass = Class.forName(designProcessVariable.getMetaClassName());
                        try {
                            value = enumClass.getMethod("fromId", String.class).invoke(null, stringValue);
                        } catch (NoSuchMethodException ex) {
                            try {
                                value = enumClass.getMethod("fromId", Integer.class).invoke(null, Integer.parseInt(stringValue));
                            } catch (NoSuchMethodException e) {
                                log.error(e);
                            }
                        }
                    } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException e) {
                        log.error(e);
                    }
                    break;

                default:

            }
        } catch (ParseException e) {
            log.error(e);
            return null;
        } catch (NumberFormatException e) {
            log.error(e);
            return null;
        }
        return value;
    }

    @Override
    public String getLocalizedValue(AbstractProcessVariable designProcessVariable) {
        String value = designProcessVariable.getValue();
        if (designProcessVariable.getAttributeType() != null && StringUtils.isNotBlank(designProcessVariable.getValue())) {
            Object object = getValue(designProcessVariable);
            if (object != null) {
                switch (designProcessVariable.getAttributeType()) {
                    case DATE:
                    case DATE_TIME:
                        value = Datatypes.get(Date.class).format((Date) object);
                        break;

                    case BOOLEAN:
                        value = getMessage(value);
                        break;

                    case ENTITY:
                        if (!PersistenceHelper.isNew(object)) value = InstanceUtils.getInstanceName((Instance) object);
                        break;

                    case ENUM:
                        value = AppBeans.get(Messages.class).getMessage((Enum) object);
                        break;

                    default:
                }
            }
        }

        return value;
    }

    @Override
    public Map<String, CardVariable> getVariablesForCard(Card card) {

        Map<String, CardVariable> processVariableMap = new HashMap<>();
        for (CardVariable cardVariable : card.getCardVariables()) {
            processVariableMap.put(cardVariable.getAlias(), cardVariable);
        }
        return processVariableMap;
    }

    public void createVariablesForCard(Card card) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        List<CardVariable> cardVariables = new ArrayList<CardVariable>();
        Map<String, AbstractProcessVariable> variableMap = collectVariablesForCard(card);
        List<String> errors = checkVariables(variableMap.values());
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String error : errors) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(error);
            }
            throw new IllegalStateException(sb.toString());
        }

        Map<String, AbstractProcessVariable> designVariables = getDesignVariables(card, new HashMap<String, AbstractProcessVariable>());

        for (String key : variableMap.keySet()) {
            CardVariable cardVariable = (CardVariable) variableMap.get(key).copyTo(new CardVariable());
            cardVariable.setCard(card);
            if (designVariables.containsKey(key)) {
                DesignProcessVariable designProcessVariable = (DesignProcessVariable) designVariables.get(key);
                cardVariable.setModuleName(designProcessVariable.getModuleName());
                designProcessVariable.setPropertyName(designProcessVariable.getPropertyName());
            }
            em.persist(cardVariable);
            cardVariables.add(cardVariable);
        }
        card.setCardVariables(cardVariables);
    }

    public List<String> checkVariables(Collection<AbstractProcessVariable> variablesForCard) {
        List<String> errorsList = new ArrayList<String>();
        for (AbstractProcessVariable processVariable : variablesForCard) {
            if (processVariable instanceof DesignProcessVariable) {
                DesignProcessVariable designProcessVariable = (DesignProcessVariable) processVariable;
                if (BooleanUtils.isTrue(designProcessVariable.getShouldBeOverridden())) {
                    errorsList.add(String.format("Variable \"%s\" should be overridden", designProcessVariable.getName()));
                }
            }
        }
        return errorsList;
    }

    @Override
    public Map<String, AbstractProcessVariable> collectVariablesForCard(Card card) {
        Map<String, AbstractProcessVariable> processVariableMap = new HashMap<>();
        getDesignVariables(card, processVariableMap);
        getProcVariables(card, processVariableMap);
        return processVariableMap;
    }

    protected Map<String, AbstractProcessVariable> getDesignVariables(Card card, Map<String, AbstractProcessVariable> processVariableMap) {
        Design design = card.getProc().getDesign();
        if (design == null || design.getDesignProcessVariables() == null) return processVariableMap;
        for (DesignProcessVariable designProcessVariable : design.getDesignProcessVariables()) {
            processVariableMap.put(designProcessVariable.getAlias(), designProcessVariable);
        }
        return processVariableMap;
    }

    protected Map<String, AbstractProcessVariable> getProcVariables(Card card, Map<String, AbstractProcessVariable> processVariableMap) {
        if (card.getProc().getProcessVariables() == null) return processVariableMap;
        for (ProcVariable procVariable : card.getProc().getProcessVariables()) {
            processVariableMap.put(procVariable.getAlias(), procVariable);
        }
        return processVariableMap;
    }

    private String getMessage(String id) {
        return AppBeans.get(Messages.class).getMessage(ProcessVariableServiceBean.class, id);
    }
}
