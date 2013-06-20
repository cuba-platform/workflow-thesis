/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.gui.base.action;

import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.entity.CardRole;
import com.haulmont.workflow.core.entity.ProcRole;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author pavlov
 * @version $Id$
 */
public class CardRolesFrameHelper {

    public static Set<String> getEmptyRolesNames(Card card, Set<CardRole> cardRoles, String requiredRolesCodesStr, List<String> deletedEmptyRoleCodes) {
        Set<String> emptyRolesNames = new HashSet<>();
        Map<String, String> procRolesNames = new HashMap<>();
        List<ProcRole> procRoles = card.getProc().getRoles();
        if (procRoles == null) {
            LoadContext ctx = new LoadContext(ProcRole.class);
            LoadContext.Query query = ctx.setQueryString("select pr from wf$ProcRole pr where pr.proc.id = :proc");
            query.setParameter("proc", card.getProc());
            procRoles = ((DataService) AppBeans.get(DataService.NAME)).loadList(ctx);
        }
        for (ProcRole procRole : procRoles) {
            procRolesNames.put(procRole.getCode(), procRole.getName());
        }

        //if we removed required role from datasource
        Set<String> emptyRequiredRolesCodes = new HashSet<>();

        if (StringUtils.isNotEmpty(requiredRolesCodesStr)) {
            String[] s = requiredRolesCodesStr.split("\\s*,\\s*");
            emptyRequiredRolesCodes = new LinkedHashSet<String>(Arrays.asList(s));
        }

        Set<String> requiredRolesChoiceCodes = new HashSet<>();
        for (String requiredRoleCode : emptyRequiredRolesCodes) {
            if (requiredRoleCode.contains("|"))
                requiredRolesChoiceCodes.add(requiredRoleCode);
        }

        for (CardRole cardRole : cardRoles) {
            if (cardRole.getUser() == null && (deletedEmptyRoleCodes == null ||
                    !deletedEmptyRoleCodes.contains(cardRole.getCode()))) {
                emptyRolesNames.add(procRolesNames.get(cardRole.getCode()));
            }

            if (!requiredRolesChoiceCodes.isEmpty()) {
                String choiceRole = null;
                for (String requiredRolesChoiceCode : requiredRolesChoiceCodes) {
                    String[] roles = requiredRolesChoiceCode.split("\\|");
                    if (Arrays.binarySearch(roles, cardRole.getCode()) >= 0) {
                        choiceRole = requiredRolesChoiceCode;
                        break;
                    }
                }

                if (choiceRole != null) {
                    requiredRolesChoiceCodes.remove(choiceRole);
                    emptyRequiredRolesCodes.remove(choiceRole);
                }
            }

            emptyRequiredRolesCodes.remove(cardRole.getCode());
        }

        for (String roleCode : emptyRequiredRolesCodes) {
            if (roleCode.contains("|")) {
                String formattingCode = "";
                String orStr = " " + ((Messages) AppBeans.get(Messages.NAME)).getMessage(CardRolesFrameHelper.class, "actorNotDefined.or") + " ";
                String[] roles = roleCode.split("\\|");
                for (String role : roles) {
                    formattingCode += procRolesNames.get(role) + orStr;
                }

                if (formattingCode.endsWith(orStr))
                    formattingCode = formattingCode.substring(0, formattingCode.lastIndexOf(orStr));

                emptyRolesNames.add(formattingCode);
            } else {
                emptyRolesNames.add(procRolesNames.get(roleCode));
            }
        }

        return emptyRolesNames;
    }
}
