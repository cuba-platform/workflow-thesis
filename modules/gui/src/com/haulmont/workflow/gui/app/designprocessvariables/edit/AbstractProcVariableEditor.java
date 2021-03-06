/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

/**
 *
 * <p>$Id: AbstractProcVariableEditor.java 11029 2013-04-03 08:15:19Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
package com.haulmont.workflow.gui.app.designprocessvariables.edit;

import com.haulmont.bali.datastruct.Pair;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.app.PersistenceManagerService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.MetadataHelper;
import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.components.validators.DateValidator;
import com.haulmont.cuba.gui.components.validators.DoubleValidator;
import com.haulmont.cuba.gui.components.validators.IntegerValidator;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.ValueListener;
import com.haulmont.cuba.gui.data.impl.CollectionDsListenerAdapter;
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory;
import com.haulmont.workflow.core.app.ProcessVariableService;
import com.haulmont.workflow.core.entity.AbstractProcessVariable;
import com.haulmont.workflow.core.enums.AttributeType;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractProcVariableEditor extends AbstractEditor {

    private static final long serialVersionUID = -2632532733737475722L;

    private String elements;

    private String initialValue;

    private IntegerValidator integerValidator;
    private DoubleValidator doubleValidator;
    private DateValidator dateValidator;

    protected AbstractProcessVariable processVariable;

    private TextField stringValueField;

    private DateField dateValueField;

    private CheckBox booleanValueField;

    private LookupField lookupValueField;

    private PickerField actionsFieldValueField;

    private LookupField metaClassNameField;

    private CheckBox useLookupCheckBox;

    private Boolean useLookup = true;

    private Boolean alreadyInitiated = false;

    @Inject
    private FieldGroup designProcessFields;

    @Inject
    private PersistenceManagerService persistenceManager;

    @Inject
    private Datasource<AbstractProcessVariable> processVariableDs;

    @Inject
    protected ProcessVariableService processVariableService;

    @Inject
    private ComponentsFactory componentsFactory;


    public AbstractProcVariableEditor() {
        super();
        elements = "stringValue,dateValue,dateTimeValue,booleanValue,lookupValue,actionsFieldValue,metaClassName,useLookup";
        integerValidator = new IntegerValidator();
        doubleValidator = new DoubleValidator();
        dateValidator = new DateValidator();
    }

    public void init(Map<String, Object> params) {
        super.init(params);
        createCustomfields();
        addListenerToEntity();
        addListenerToMetaClass();
    }

    private void addListenerToMetaClass() {
        metaClassNameField.addListener(new ValueListener() {
            public void valueChanged(Object source, String property, Object prevValue, Object value) {
                if (processVariable != null) {
                    processVariable.setMetaClassName((String) value);
                }
            }
        });
    }

    private Boolean canSetValue() {
        Boolean canSetValue = processVariable.getAttributeType() != null;
        if (Arrays.asList(AttributeType.ENTITY, AttributeType.ENUM).contains(processVariable.getAttributeType()) && StringUtils.isBlank(processVariable.getMetaClassName())) {
            canSetValue = false;
        }
        return canSetValue;
    }

    private void addListenerToEntity() {

        processVariableDs.addListener(new CollectionDsListenerAdapter<AbstractProcessVariable>() {
            @Override
            public void valueChanged(AbstractProcessVariable source, String property, Object prevValue, Object value) {
                if ("attributeType".equals(property)) {
                    setFields();
                    if (!Arrays.asList(AttributeType.ENTITY, AttributeType.ENUM).contains(processVariable.getAttributeType())) {
                        processVariable.setMetaClassName(null);
                        metaClassNameField.setValue(null);
                    }
                    setValueToFields();
                }
                if ("metaClassName".equals(property)) {
                    if (processVariable.getMetaClassName() != null && processVariable.getAttributeType() != null) {
                        reloadOptionsDatasource();
                        setValueToFields();
                    } else {
                        actionsFieldValueField.setEnabled(false);
                        lookupValueField.setEnabled(false);
                    }
                }
            }
        });
    }

    private void reloadOptionsDatasource() {
        if (AttributeType.ENTITY.equals(processVariable.getAttributeType())) {
            setCollectionDSonDefaultValue(processVariable.getMetaClassName());
        }
        if (AttributeType.ENUM.equals(processVariable.getAttributeType())) {
            Class enumClass = null;
            try {
                enumClass = Class.forName(processVariable.getMetaClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Unrecognized enum class name", e);
            }
            setCollectionDSonEnumDefaultValue(enumClass);
        }
    }

    private void createCustomfields() {
        createStringField();
        createDateField();
        createBooleanField();
        createLookupField();
        createActionsField();
        createMetaclassNameField();
        createUseLookupButton();
    }

    private void createMetaclassNameField() {
        designProcessFields.addCustomField("metaClassName", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                metaClassNameField = componentsFactory.createComponent(LookupField.NAME);
                return metaClassNameField;
            }
        });
        designProcessFields.setRequired("metaClassName", true, getMessage("metaClass") + " " + getMessage("required"));
    }

    private void createUseLookupButton() {
        designProcessFields.addCustomField("useLookup", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                useLookupCheckBox = componentsFactory.createComponent(CheckBox.NAME);
                useLookupCheckBox.setValue(useLookup);
                useLookupCheckBox.addListener(new ValueListener() {
                    public void valueChanged(Object source, String property, Object prevValue, Object value) {
                        useLookup = BooleanUtils.isTrue((Boolean) value);
                        if (AttributeType.ENTITY.equals(processVariable.getAttributeType())) {
                            setFields();
                            if (StringUtils.isNotBlank(processVariable.getMetaClassName())) {
                                setCollectionDSonDefaultValue(processVariable.getMetaClassName());
                                setValueToFields();
                            }
                        }

                    }
                });
                return useLookupCheckBox;
            }

        });
    }

    private void createActionsField() {
        designProcessFields.addCustomField("actionsFieldValue", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                actionsFieldValueField = componentsFactory.createComponent(PickerField.NAME);
                return actionsFieldValueField;
            }
        });
    }

    private void createLookupField() {
        designProcessFields.addCustomField("lookupValue", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                lookupValueField = componentsFactory.createComponent(LookupField.NAME);
                return lookupValueField;
            }
        });
    }

    private void createBooleanField() {
        designProcessFields.addCustomField("booleanValue", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                booleanValueField = componentsFactory.createComponent(CheckBox.NAME);
                return booleanValueField;
            }
        });
    }

    private void createDateField() {
        designProcessFields.addCustomField("dateValue", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                dateValueField = componentsFactory.createComponent(DateField.NAME);
                dateValueField.setResolution(DateField.Resolution.DAY);
                return dateValueField;
            }
        });
    }

    private void createStringField() {
        designProcessFields.addCustomField("stringValue", new FieldGroup.CustomFieldGenerator() {
            public Component generateField(Datasource datasource, String propertyId) {
                stringValueField = componentsFactory.createComponent(TextField.NAME);
                return stringValueField;
            }
        });
    }

    private void setFields() {
        if (processVariable.getAttributeType() != null) {
            setVisibleForElemnts(false);
            switch (processVariable.getAttributeType()) {

                case INTEGER:
                    setVisibleString(integerValidator);
                    break;

                case DOUBLE:
                    setVisibleString(doubleValidator);
                    break;

                case STRING:
                    setVisibleString(null);
                    break;

                case DATE:
                    setVisibleDate(DateField.Resolution.DAY);
                    break;

                case DATE_TIME:
                    setVisibleDate(DateField.Resolution.MIN);
                    break;

                case BOOLEAN:
                    setVisibleBoolean();
                    break;

                case ENTITY:
                    setVisiblePickerField();
                    break;

                case ENUM:
                    setVisibleLookup();
                    break;

                default:
                    setVisibleString(null);
                    break;

            }
        }
    }

    private void setCollectionDSonDefaultValue(String metaClassName) {
        MetaClass metaClass = MetadataProvider.getSession().getClass(metaClassName);
        if (!useLookup) {
            actionsFieldValueField.setEnabled(true);
            actionsFieldValueField.setMetaClass(metaClass);
        } else {
            lookupValueField.setEnabled(true);
            LoadContext loadContext = new LoadContext(metaClass);
            String queryString = String.format("select e from %s e", metaClassName);
            Pair<String, Map<String, Object>> where = getWhereCondition(metaClassName);
            if (where != null) {
                queryString += " where " + where.getFirst();
            }
            loadContext.setQueryString(queryString);
            if (where != null) {
                for (String key : where.getSecond().keySet()) {
                    loadContext.getQuery().setParameter(key, where.getSecond().get(key));
                }
            }
            loadContext.getQuery().setMaxResults(persistenceManager.getMaxFetchUI(metaClassName));
            List<Entity> optionsList = getDsContext().getDataSupplier().loadList(loadContext);
            lookupValueField.setOptionsList(optionsList);
        }
    }

    protected Pair<String, Map<String, Object>> getWhereCondition(String metaClassName) {
        return null;
    }

    private void setCollectionDSonEnumDefaultValue(Class enumClass) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            for (Enum value : (Enum[]) enumClass.getMethod("values").invoke(null)) {
                map.put(MessageProvider.getMessage(value), value);
            }
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException("Can't get values from enum " + enumClass.getCanonicalName(), e);
        }
        lookupValueField.setOptionsMap(map);
        lookupValueField.setEnabled(true);
    }

    public void setVisibleForElemnts(boolean visible) {
        setVisibleForElemnts(elements, visible);
    }

    private void setVisibleForElemnts(String elements, boolean visible) {
        for (String fieldName : elements.split(",")) {
            if (designProcessFields.getField(fieldName) != null) {
                designProcessFields.setVisible(fieldName, visible);
            }
        }
    }

    public void removeValidators(Field field) {
        field.removeValidator(integerValidator);
        field.removeValidator(doubleValidator);
        field.removeValidator(dateValidator);
    }

    public void setVisibleString(Field.Validator validator) {
        designProcessFields.setVisible("stringValue", true);
        removeValidators(stringValueField);
        if (validator != null) {
            stringValueField.addValidator(validator);
        }
    }

    public void setVisibleDate(DateField.Resolution resolution) {
        designProcessFields.setVisible("dateValue", true);
        removeValidators(dateValueField);
        dateValueField.setResolution(resolution);
        dateValueField.addValidator(dateValidator);
    }

    public void setVisibleBoolean() {
        designProcessFields.setVisible("booleanValue", true);
    }

    public void setVisibleLookup() {
        designProcessFields.setVisible("lookupValue", true);
        designProcessFields.setVisible("metaClassName", !alreadyInitiated);
        Map<String, Object> map = new HashMap<String, Object>();
        for (Class enumClass : MetadataHelper.getAllEnums()) {
            String key = MessageProvider.getMessage(enumClass.getPackage().getName(), enumClass.getSimpleName());
            map.put(key, enumClass.getName());
        }
        String className = processVariable.getMetaClassName();
        metaClassNameField.setOptionsMap(map);
        metaClassNameField.setValue(className);
    }

    public void setVisiblePickerField() {
        designProcessFields.setVisible(useLookup ? "lookupValue" : "actionsFieldValue", true);
        designProcessFields.setVisible("metaClassName", !alreadyInitiated);
        designProcessFields.setVisible("useLookup", true);
        Map<String, Object> map = new HashMap<String, Object>();
        for (MetaClass cl : MetadataHelper.getAllPersistentMetaClasses()) {
            String key = MessageProvider.getMessage(cl.getJavaClass().getPackage().getName(), cl.getJavaClass().getSimpleName());
            map.put(key, cl.getName());
        }
        String className = processVariable.getMetaClassName();
        metaClassNameField.setOptionsMap(map);
        metaClassNameField.setValue(className);
    }

    public void commitAndClose() {
        Object value = processVariable.getValue();
        if (processVariable.getAttributeType() != null) {
            switch (processVariable.getAttributeType()) {
                case INTEGER:
                case DOUBLE:
                case STRING:
                    value = stringValueField.getValue();
                    break;

                case DATE:
                case DATE_TIME:
                    value = dateValueField.getValue();
                    break;

                case BOOLEAN:
                    value = booleanValueField.getValue();
                    break;

                case ENTITY:
                    value = useLookup ? lookupValueField.getValue() : actionsFieldValueField.getValue();
                    break;

                case ENUM:
                    value = lookupValueField.getValue();
                    break;

                default:
                    value = stringValueField.getValue();
                    break;

            }
        }
        String newValue = processVariableService.getStringValue(value);
        if (!ObjectUtils.equals(initialValue, newValue)) {
            processVariable.setOverridden(true);
        }
        processVariable.setValue(newValue);
        if (checkProcessValue()) {
            super.commitAndClose();
        }
    }

    protected boolean checkProcessValue() {
        return true;
    }

    public void setItem(Entity item) {
        super.setItem(item);
        processVariable = (AbstractProcessVariable) getItem();
        initialValue = processVariable.getValue();
        setVisibleForElemnts(false);
        if (processVariable.getAttributeType() != null) {
            designProcessFields.setEditable("attributeType", false);
            designProcessFields.setEditable("alias", false);
            designProcessFields.setVisible("metaClassName", false);
            alreadyInitiated = true;
        } else {
            processVariable.setAttributeType(AttributeType.STRING);
        }

        setFields();
        reloadOptionsDatasource();
        setValueToFields();
    }

    private void setValueToFields() {
        if (canSetValue()) {
            try {
                Object value = processVariableService.getValue(processVariable);

                if (processVariable.getAttributeType() != null)
                    switch (processVariable.getAttributeType()) {

                        case INTEGER:
                        case DOUBLE:
                        case STRING:
                            stringValueField.setValue(value != null ? value.toString() : null);
                            break;

                        case DATE:
                        case DATE_TIME:
                            dateValueField.setValue(value);
                            break;

                        case BOOLEAN:
                            booleanValueField.setValue(value);
                            break;

                        case ENTITY:
                            if (useLookup) {
                                lookupValueField.setValue(value);
                            } else {
                                actionsFieldValueField.setValue(value);
                            }
                            break;

                        case ENUM:
                            lookupValueField.setValue(value);
                            break;

                        default:
                            //Default Task
                            break;
                    }
            } catch (Exception e) {
                showNotification(getMessage("wrongAttributeType"), NotificationType.WARNING);
            }
        }
    }
}
