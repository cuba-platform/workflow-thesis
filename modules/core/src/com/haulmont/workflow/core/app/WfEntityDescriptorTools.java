/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.chile.core.datatypes.Datatypes;
import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.chile.core.model.Instance;
import com.haulmont.chile.core.model.utils.InstanceUtils;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.global.WfEntityDescriptor;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * @author zaharchenko
 * @version $Id$
 */
@ManagedBean(WfEntityDescriptorTools.NAME)
public class WfEntityDescriptorTools {

    public static final String NAME = "workflow_WfDynamicEntityProvider";

    @Inject
    protected Persistence persistence;
    @Inject
    protected Metadata metadata;
    @Inject
    protected Messages messages;

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
                    Class entityClass = metadata.getSession()
                            .getClass(wfEntityDescriptor.getMetaClassName()).getJavaClass();
                    Transaction transaction = persistence.createTransaction();
                    Entity entity = null;
                    try {
                        EntityManager em = persistence.getEntityManager();
                        entity = em.find(entityClass, UUID.fromString(stringValue));
                        transaction.commit();
                    } finally {
                        transaction.end();
                    }
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
                        value = Datatypes.get(Date.class).format((Date) object);
                        break;

                    case BOOLEAN:
                        value = messages.getMessage(WfEntityDescriptorTools.class, value);
                        break;

                    case ENTITY:
                        if (!PersistenceHelper.isNew(object)) value = InstanceUtils.getInstanceName((Instance) object);
                        break;

                    case ENUM:
                        value = messages.getMessage((Enum) object);
                        break;

                    default:
                }
            }
        }

        return value;
    }


}
