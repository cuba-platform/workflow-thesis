/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.enums.AttributeType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Map;

/**
 * @author zaharchenko
 * @version $Id$
 */
@Service(CardPropertyHandlerLoaderService.NAME)
public class CardPropertyHandlerLoaderServiceBean implements CardPropertyHandlerLoaderService {

    @Inject
    private CardPropertyHandlerLoader handlerLoader;

    @Override
    public String getLocalizedValue(Class clazz, Boolean useExpression, String value) {
        return handlerLoader.loadHandler(clazz, null, useExpression).getLocalizedValue(value);
    }

    @Override
    public AttributeType getAttributeType(Class clazz, Boolean useExpression) {
        return handlerLoader.loadHandler(clazz, null, useExpression).getAttributeType();
    }

    @Override
    public Map<String, Object> loadObjects(Class clazz, Boolean useExpression) {
        return handlerLoader.loadHandler(clazz, null, useExpression).loadObjects();
    }
}
