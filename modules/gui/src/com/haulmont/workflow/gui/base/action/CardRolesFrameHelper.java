/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.base.action;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.app.cardroles.CardRolesFrameWorker;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;

import java.util.List;
import java.util.Set;

/**
 * Deprecated - use {@link com.haulmont.workflow.core.app.cardroles.CardRolesFrameWorker}
 *
 * @author pavlov
 * @version $Id$
 * @version $Id$
 */
@Deprecated
public class CardRolesFrameHelper {

    public static Set<String> getEmptyRolesNames(Card card, Set<CardRole> cardRoles, String requiredRolesCodesStr,
                                                 List<String> deletedEmptyRoleCodes) {

        return AppBeans.get(CardRolesFrameWorker.class).getEmptyRolesNames(card, cardRoles, requiredRolesCodesStr,
                deletedEmptyRoleCodes);
    }
}
