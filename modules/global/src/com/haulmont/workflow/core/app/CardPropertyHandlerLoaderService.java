/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.enums.AttributeType;

import java.util.Map;

/**
 * @author zaharchenko
 * @version $Id$
 */
public interface CardPropertyHandlerLoaderService {

    String NAME = "workflow_CardPropertyHandlerLoaderService";

    String getLocalizedValue(Class clazz, Boolean useExpression, String value);

    AttributeType getAttributeType(Class clazz, Boolean useExpression);

    Map<String, Object> loadObjects(Class clazz, Boolean useExpression);
}
