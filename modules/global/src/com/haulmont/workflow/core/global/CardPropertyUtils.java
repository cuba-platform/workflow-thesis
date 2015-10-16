/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.global;

import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.chile.core.model.MetaProperty;
import com.haulmont.chile.core.model.Range;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.View;
import com.haulmont.workflow.core.enums.AttributeType;
import com.haulmont.workflow.core.enums.OperationsType;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;

/**
 * @author zaharchenko
 * @version $Id$
 */
public final class CardPropertyUtils {

    private CardPropertyUtils() {
    }

    public static AttributeType getAttributeTypeFromClass(Class clazz) {
        if (Integer.class.isAssignableFrom(clazz)) return AttributeType.INTEGER;
        if (Double.class.isAssignableFrom(clazz)) return AttributeType.DOUBLE;
        if (BigDecimal.class.isAssignableFrom(clazz)) return AttributeType.DOUBLE;
        if (Date.class.isAssignableFrom(clazz)) return AttributeType.DATE_TIME;
        if (String.class.isAssignableFrom(clazz)) return AttributeType.STRING;
        if (Boolean.class.isAssignableFrom(clazz)) return AttributeType.BOOLEAN;
        if (EnumClass.class.isAssignableFrom(clazz)) return AttributeType.ENUM;
        if (BaseUuidEntity.class.isAssignableFrom(clazz)) return AttributeType.ENTITY;
        return null;
    }

    public static Class getSimpleClassFromAttributeType(AttributeType attributeType) {
        if (attributeType == null) {
            return null;
        }
        switch (attributeType) {
            case BOOLEAN:
                return Boolean.class;
            case STRING:
                return String.class;
            case INTEGER:
                return Integer.class;
            case DOUBLE:
                return Double.class;
            case DATE:
            case DATE_TIME:
                return Date.class;
            case ENUM:
                return EnumClass.class;
            case ENTITY:
                return BaseUuidEntity.class;

        }
        return null;
    }

    public static void generateViewByPropertyPath(View view, String path) {
        String propertyPath = StringUtils.substringBefore(path, ".");
        MetaClass metaClass = ((Metadata) AppBeans.get(Metadata.NAME)).getSession().getClass(view.getEntityClass());
        MetaProperty metaProperty = metaClass.getProperty(propertyPath);
        if (metaProperty == null) {
            throw new RuntimeException("Path '" + propertyPath + "' not exists in property path '" + path + "'");
        }
        String newPath = StringUtils.substringAfter(path, ".");
        if (propertyPath.equals(path) || Arrays.asList(MetaProperty.Type.ENUM, MetaProperty.Type.DATATYPE).contains(metaProperty.getType())) {
            if (Arrays.asList(MetaProperty.Type.ENUM, MetaProperty.Type.DATATYPE).contains(metaProperty.getType())) {
                view.addProperty(propertyPath);
            } else {
                MetaClass propertyMetaClass = ((Metadata) AppBeans.get(Metadata.NAME)).getSession().getClass(metaProperty.getJavaType());
                View propertyView = new View(propertyMetaClass.getJavaClass(), View.MINIMAL);
                view.addProperty(propertyPath, propertyView);
            }
            return;
        }
        MetaClass newMetaClass = ((Metadata) AppBeans.get(Metadata.NAME)).getSession().getClass(metaProperty.getJavaType());
        View propertyView = new View(newMetaClass.getJavaClass());
        view.addProperty(propertyPath, propertyView);
        generateViewByPropertyPath(propertyView, newPath);
    }

    public static Class getClassByMetaProperty(MetaClass metaClass, String path) {
        String propertyPath = StringUtils.substringBefore(path, ".");
        MetaProperty metaProperty = metaClass.getProperty(propertyPath);
        if (metaProperty == null) {
            return null;
        }
        String newPath = StringUtils.substringAfter(path, ".");
        if (propertyPath.equals(path) || Arrays.asList(MetaProperty.Type.ENUM, MetaProperty.Type.DATATYPE).contains(metaProperty.getType())) {
            return metaProperty.getJavaType();
        }
        MetaClass newMetaClass = ((Metadata) AppBeans.get(Metadata.NAME)).getSession().getClass(metaProperty.getJavaType());
        return getClassByMetaProperty(newMetaClass, newPath);
    }

    public static String getSystemPathByMetaProperty(MetaClass metaClass, String path) {
        String propertyPathName = StringUtils.substringBefore(path, ".");
        String propertyPath = propertyPathName;
        MetaProperty metaProperty = metaClass.getProperty(propertyPathName);
        if (metaProperty == null) {
            propertyPath = findPropertyPath(metaClass, propertyPathName);
            if (propertyPath != null) {
                metaProperty = metaClass.getProperty(propertyPath);
            }
        }
        if (metaProperty == null) {
            return null;
        }
        String newPath = StringUtils.substringAfter(path, ".");
        if (propertyPathName.equals(path) || Arrays.asList(MetaProperty.Type.ENUM, MetaProperty.Type.DATATYPE).contains(metaProperty.getType())) {
            return propertyPath;
        }
        MetaClass newMetaClass = ((Metadata) AppBeans.get(Metadata.NAME)).getSession().getClass(metaProperty.getJavaType());
        String append = getSystemPathByMetaProperty(newMetaClass, newPath);
        if (append != null) {
            return propertyPath + "." + append;
        }
        return null;
    }

    public static String findPropertyPath(MetaClass metaClass, String propertyPath) {
        String formattedPropertyPath = propertyPath.replace(".", "");
        Messages messages = AppBeans.get(Messages.NAME);
        for (MetaProperty property : metaClass.getProperties()) {
            String propertyName = property.getName();
            Class declaringClass = property.getDeclaringClass();
            String localizePropertyName = propertyName;
            if (declaringClass != null) {
                localizePropertyName = messages.getMessage(declaringClass.getPackage().getName(),
                        declaringClass.getSimpleName() + "." + propertyName)
                        .replace(".", "");
            }
            if (!Arrays.asList(Range.Cardinality.MANY_TO_MANY, Range.Cardinality.ONE_TO_MANY).contains(property.getRange().getCardinality())) {
                if (formattedPropertyPath.equals(localizePropertyName)) {
                    return propertyName;
                }
            }
        }
        return null;
    }

    public static boolean compareValue(OperationsType operationsType, Object targetValue, Object value) {
        if (targetValue == null) return operationsType.equals(OperationsType.DOES_NOT_CONTAIN);
        switch (operationsType) {
            case EQUAL:
                return value.equals(targetValue);
            case NOT_EQUAL:
                return !value.equals(targetValue);
            case GREATER:
                if (value instanceof Date) {
                    return ((Date) targetValue).after((Date) value);
                }
                if (value instanceof Integer) {
                    return (Integer) targetValue > (Integer) value;
                }
                if (value instanceof Double) {
                    return (Double) targetValue > (Double) value;
                }
                if (value instanceof BigDecimal) {
                    return more((BigDecimal) targetValue, (BigDecimal) value);
                }
                break;
            case GREATER_OR_EQUAL:
                if (value instanceof Date) {
                    return ((Date) targetValue).after((Date) value) || targetValue.equals(value);
                }
                if (value instanceof Integer) {
                    return (Integer) targetValue >= (Integer) value;
                }
                if (value instanceof Double) {
                    return (Double) targetValue >= (Double) value;
                }
                if (value instanceof BigDecimal) {
                    return moreOrEqual((BigDecimal) targetValue, (BigDecimal) value);
                }
                break;
            case LESSER:
                if (value instanceof Date) {
                    return ((Date) targetValue).before((Date) value);
                }
                if (value instanceof Integer) {
                    return (Integer) targetValue < (Integer) value;
                }
                if (value instanceof Double) {
                    return (Double) targetValue < (Double) value;
                }
                if (value instanceof BigDecimal) {
                    return less((BigDecimal) targetValue, (BigDecimal) value);
                }
                break;
            case LESSER_OR_EQUAL:
                if (value instanceof Date) {
                    return ((Date) targetValue).before((Date) value) || targetValue.equals(value);
                }
                if (value instanceof Integer) {
                    return (Integer) targetValue <= (Integer) value;
                }
                if (value instanceof Double) {
                    return (Double) targetValue <= (Double) value;
                }
                if (value instanceof BigDecimal) {
                    return lessOrEqual((BigDecimal) targetValue, (BigDecimal) value);
                }
                break;
            case CONTAINS:
                if (value instanceof String) {
                    return ((String) targetValue).contains((String) value);
                }
                break;
            case DOES_NOT_CONTAIN:
                if (value instanceof String) {
                    return !((String) targetValue).contains((String) value);
                }
                break;
            case STARTS_WITH:
                if (value instanceof String) {
                    return ((String) targetValue).startsWith((String) value);
                }
                break;
            case ENDS_WITH:
                if (value instanceof String) {
                    return ((String) targetValue).endsWith((String) value);
                }
                break;
        }
        return false;
    }

    private static boolean lessOrEqual(BigDecimal first, BigDecimal second) {
        Boolean x = checkArguments(first, second);
        if (x != null) return x;
        return first.compareTo(second) <= 0;
    }

    private static Boolean checkArguments(BigDecimal first, BigDecimal second) {
        if (first == null && second == null) return false;
        if (first == null) return false;
        if (second == null) return true;
        return null;
    }

    private static boolean less(BigDecimal first, BigDecimal second) {
        Boolean x = checkArguments(first, second);
        if (x != null) return x;
        return first.compareTo(second) < 0;
    }

    private static boolean moreOrEqual(BigDecimal first, BigDecimal second) {
        Boolean x = checkArguments(first, second);
        if (x != null) return x;
        return first.compareTo(second) >= 0;
    }

    private static boolean more(BigDecimal first, BigDecimal second) {
        Boolean x = checkArguments(first, second);
        if (x != null) return x;
        return first.compareTo(second) > 0;
    }
}
