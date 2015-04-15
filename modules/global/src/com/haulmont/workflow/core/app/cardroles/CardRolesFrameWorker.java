/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.cardroles;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRole;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author stekolschikov
 * @version $Id$
 */
public interface CardRolesFrameWorker {

    String NAME = "workflow_CardRolesFrameWorker";

    Set<String> getEmptyRolesNames(Card card, Set<CardRole> cardRoles, String requiredRolesCodesStr,
                                   List<String> deletedEmptyRoleCodes);

    void assignNextSortOrder(CardRole nextCardRole, Collection<CardRole> allCardRoles);

    void normalizeSortOrders(Proc currentProcess, boolean combinedStagesEnabled, Collection<ProcRole> procRoles,
                             Collection<CardRole> cardRoles);

    List<CardRole> getAllCardRolesWithProcRole(ProcRole procRole, Collection<CardRole> cardRoles);

    int getMaxSortOrderInCardRoles(List<CardRole> cardRoles);
}
