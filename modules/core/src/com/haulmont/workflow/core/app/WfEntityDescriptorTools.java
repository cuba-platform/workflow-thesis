/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.global.WfEntityDescriptor;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 */
@Component(WfEntityDescriptorTools.NAME)
public class WfEntityDescriptorTools {

    public static final String NAME = "workflow_WfDynamicEntityProvider";

    public String getStringValue(Object value) {
        String stringValue = null;
        if (value != null)
            if (value instanceof Integer)
                stringValue = Datatypes.getNN(Integer.class).format(value);
            else if (value instanceof BigDecimal)
                stringValue = Datatypes.getNN(BigDecimal.class).format(value);
            else if (value instanceof Double)
                stringValue = Datatypes.getNN(Double.class).format(value);
            else if (value instanceof Date)
                stringValue = Long.valueOf(((Date) value).getTime()).toString();
            else if (value instanceof Entity)
                stringValue = ((Entity) value).getId().toString();
            else if (value instanceof UUID)
                stringValue = value.toString();
            else if (value instanceof Enum)
                stringValue = ((EnumClass) value).getId().toString();
            else
                stringValue = value.toString();
        return stringValue;
    }

    public Object getValue(WfEntityDescriptor wfEntityDescriptor) {
        Object value = null;
        String stringValue = wfEntityDescriptor.getValue();
        if (wfEntityDescriptor.getAttributeType() == null) return stringValue;
        if (StringUtils.isBlank(stringValue)) {
            if (AttributeType.STRING == wfEntityDescriptor.getAttributeType()) {
                return stringValue;
            } else {
                return null;
            }
        }
        try {
            switch (wfEntityDescriptor.getAttributeType()) {
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
                            .getClass(wfEntityDescriptor.getMetaClassName()).getJavaClass();
                    EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
                    Entity entity = em.find(entityClass, UUID.fromString(stringValue));
                    if (entity != null) {
                        InstanceUtils.getInstanceName(entity);
                        value = entity;
                    }
                    break;

                case ENUM:
                    Class enumClass = Class.forName(wfEntityDescriptor.getMetaClassName());
                    try {
                        value = enumClass.getMethod("fromId", String.class).invoke(null, stringValue);
                    } catch (NoSuchMethodException ex) {
                        try {
                            value = enumClass.getMethod("fromId", Integer.class).invoke(null, Integer.parseInt(stringValue));
                        } catch (NoSuchMethodException e) {
                            throw new IllegalStateException(e);
                        }
                    }
                    break;

                default:

            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        return value;
    }

    public String getLocalizedValue(WfEntityDescriptor wfEntityDescriptor) {
        String value = wfEntityDescriptor.getValue();
        if (wfEntityDescriptor.getAttributeType() != null && StringUtils.isNotBlank(wfEntityDescriptor.getValue())) {
            Object object = getValue(wfEntityDescriptor);
            if (object != null) {
                switch (wfEntityDescriptor.getAttributeType()) {
                    case DATE:
                    case DATE_TIME:
                        value = Datatypes.getNN(Date.class).format(object);
                        break;

                    case BOOLEAN:
                        value = AppBeans.get(Messages.class).getMessage(WfEntityDescriptorTools.class, value);
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


}
