/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app.cardroles;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.entity.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang.StringUtils;

import org.springframework.stereotype.Component;
import java.util.*;

@Component(CardRolesFrameWorker.NAME)
public class CardRolesFrameWorkerBean implements CardRolesFrameWorker {

    @Override
    public Set<String> getEmptyRolesNames(Card card, Set<CardRole> cardRoles, String requiredRolesCodesStr,
                                          List<String> deletedEmptyRoleCodes) {

        Set<String> emptyRolesNames = new HashSet<>();
        Map<String, String> procRolesNames = new HashMap<>();
        List<ProcRole> procRoles = card.getProc().getRoles();
        if (procRoles == null) {
            LoadContext ctx = new LoadContext(ProcRole.class);
            LoadContext.Query query = ctx.setQueryString("select pr from wf$ProcRole pr where pr.proc.id = :proc");
            query.setParameter("proc", card.getProc());
            procRoles = AppBeans.get(DataManager.class).loadList(ctx);
        }
        for (ProcRole procRole : procRoles) {
            procRolesNames.put(procRole.getCode(), procRole.getName());
        }

        //if we removed required role from datasource
        Set<String> emptyRequiredRolesCodes = new HashSet<>();

        if (StringUtils.isNotEmpty(requiredRolesCodesStr)) {
            String[] s = requiredRolesCodesStr.split("\\s*,\\s*");
            emptyRequiredRolesCodes = new LinkedHashSet<>(Arrays.asList(s));
        }

        Set<String> requiredRolesChoiceCodes = new HashSet<>();
        for (String requiredRoleCode : emptyRequiredRolesCodes) {
            if (requiredRoleCode.contains("|"))
                requiredRolesChoiceCodes.add(requiredRoleCode);
        }

        for (final CardRole cardRole : cardRoles) {
            if (isCardRoleEmpty(cardRole, deletedEmptyRoleCodes)) {
                emptyRolesNames.add(procRolesNames.get(cardRole.getCode()));
            }

            if (!requiredRolesChoiceCodes.isEmpty()) {
                String choiceRole = (String) CollectionUtils.find(requiredRolesChoiceCodes, new Predicate() {
                    @Override
                    public boolean evaluate(Object object) {
                        return Arrays.asList(((String) object).split("\\|")).contains(cardRole.getCode());
                    }
                });

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
                String orStr = " " + AppBeans.get(Messages.class).getMessage(CardRolesFrameWorkerBean.class, "CardRolesFrameWorker.actorNotDefined.or") + " ";
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

    protected boolean isCardRoleEmpty(CardRole cardRole, List<String> deletedEmptyRoleCodes) {
        return cardRole.getUser() == null && (deletedEmptyRoleCodes == null ||
                !deletedEmptyRoleCodes.contains(cardRole.getCode()));
    }

    @Override
    public void assignNextSortOrder(CardRole nextCardRole, Collection<CardRole> allCardRoles) {
        if (nextCardRole.getSortOrder() != null)
            return;
        List<CardRole> cardRolesWithProcRole = getAllCardRolesWithProcRole(nextCardRole.getProcRole(), allCardRoles);
        if (cardRolesWithProcRole.size() == 0) {
            nextCardRole.setSortOrder(1);
        } else if (nextCardRole.getProcRole().getMultiUser()) {
            int max = getMaxSortOrderInCardRoles(cardRolesWithProcRole);
            if (nextCardRole.getProcRole().getOrderFillingType() == OrderFillingType.PARALLEL) {
                nextCardRole.setSortOrder(max);
            }
            if (nextCardRole.getProcRole().getOrderFillingType() == OrderFillingType.SEQUENTIAL) {
                nextCardRole.setSortOrder(max + 1);
            }
        }
    }

    @Override
    public void normalizeSortOrders(Proc currentProcess, boolean combinedStagesEnabled,
                                    Collection<ProcRole> procRoles, Collection<CardRole> cardRoles) {

        if (currentProcess == null || !(currentProcess.getCombinedStagesEnabled() || combinedStagesEnabled))
            return;

        for (ProcRole pr : procRoles) {
            if (pr != null && pr.getMultiUser()) {
                List<CardRole> cardRolesWithProcRole = getAllCardRolesWithProcRole(pr, cardRoles);
                int index = 1;
                Map<Integer, List<CardRole>> cardRolesBySortOrder = new HashMap<>();

                for (CardRole cr : cardRolesWithProcRole) {
                    List<CardRole> cardRolesList = cardRolesBySortOrder.get(cr.getSortOrder());
                    if (cardRolesList == null) {
                        cardRolesList = new ArrayList<>();
                        cardRolesBySortOrder.put(cr.getSortOrder(), cardRolesList);
                    }
                    cardRolesList.add(cr);
                }

                for (Map.Entry<Integer, List<CardRole>> entry : cardRolesBySortOrder.entrySet()) {
                    for (CardRole cardRole : entry.getValue())
                        cardRole.setSortOrder(index);
                    index++;
                }
            }
        }
    }

    @Override
    public List<CardRole> getAllCardRolesWithProcRole(ProcRole procRole, Collection<CardRole> cardRoles) {
        List<CardRole> cardRolesWithProcRole = new ArrayList<>();
        for (CardRole cr : cardRoles) {
            if (cr.getProcRole().equals(procRole)) {
                cardRolesWithProcRole.add(cr);
            }
        }
        return cardRolesWithProcRole;
    }

    @Override
    public int getMaxSortOrderInCardRoles(List<CardRole> cardRoles) {
        int max = 0;
        for (CardRole role : cardRoles) {
            if (role.getSortOrder() != null && role.getSortOrder() > max)
                max = role.getSortOrder();
        }
        return max > cardRoles.size() ? cardRoles.size() : max;
    }
}
