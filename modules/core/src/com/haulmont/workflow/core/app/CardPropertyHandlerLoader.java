/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;


import com.haulmont.chile.core.datatypes.impl.EnumClass;
import com.haulmont.cuba.core.entity.BaseUuidEntity;
import com.haulmont.workflow.core.app.valuehandler.BaseCardPropertyHandler;
import com.haulmont.workflow.core.app.valuehandler.DateCardPropertyHandler;
import com.haulmont.workflow.core.app.valuehandler.EntityCardPropertyHandler;
import com.haulmont.workflow.core.app.valuehandler.EnumCardPropertyHandler;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.app.valuehandler.CardPropertyHandler;

import org.springframework.stereotype.Component;
import java.util.Date;

/**
 */
@Component(CardPropertyHandlerLoader.NAME)
public class CardPropertyHandlerLoader {

    public static final String NAME = "workflow_WfCardPropertyValueHandlerLoader";

    public CardPropertyHandler loadHandler(Class clazz, Card card, Boolean isExpression) {
        if (BaseUuidEntity.class.isAssignableFrom(clazz)) {
            return new EntityCardPropertyHandler(clazz, card, isExpression);
        }
        if (EnumClass.class.isAssignableFrom(clazz)) {
            return new EnumCardPropertyHandler(clazz, card, isExpression);
        }
        if (Date.class.isAssignableFrom(clazz)) {
            return new DateCardPropertyHandler(clazz, card, isExpression);
        }
        return new BaseCardPropertyHandler(clazz, card, isExpression);
    }
}
