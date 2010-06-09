/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 27.05.2010 12:33:30
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.workflow.core.entity.*;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.ProcRolePermissionValue;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service(ProcRolePermissionsService.NAME)
public class ProcRolePermissionsServiceBean implements ProcRolePermissionsService {

    private Log log = LogFactory.getLog(ProcRolePermissionsServiceBean.class);

    private List<ProcRolePermission> permissions;

    private List<Proc> processes;

    private void initPermissions() {
        log.debug("initializing ProcRolePermissionService");
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("select prp from wf$ProcRolePermission prp");
            query.setView(MetadataProvider.getViewRepository().getView(ProcRolePermission.class, "edit"));
            permissions = new ArrayList<ProcRolePermission>(query.getResultList());
            tx.commit();
        } finally {
            tx.end();
        }
    }

    private void initProcesses() {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("select p from wf$Proc p");
            query.setView(getProcView());
            processes = query.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public boolean isPermitted(CardRole cardRoleTo, String state, ProcRolePermissionType type) {
        //for not-perisisted CardRole entitites we'll allow everything
        if (PersistenceHelper.isNew(cardRoleTo)) return true;
        return isPermitted(cardRoleTo.getCard(), cardRoleTo.getProcRole(), state, type);
    }

    public boolean isPermitted(Card card, ProcRole procRoleTo, String state, ProcRolePermissionType type) {
        if (BooleanUtils.isNotTrue(procRoleTo.getProc().getPermissionsEnabled())) return true;

        if (permissions == null) initPermissions();
        if (processes == null) initProcesses();


//        for not-active processes we'll allow everything by now
        if (card.getProcs() == null) {
            Transaction tx = Locator.createTransaction();
            try {
                EntityManager em = PersistenceProvider.getEntityManager();
                em.setView(new View(Card.class, "edit").addProperty("procs"));
                card = em.find(Card.class, card.getId());
                tx.commit();
            } finally {
                tx.end();
            }
        }

        User currentUser = SecurityProvider.currentUserSession().getCurrentOrSubstitutedUser();
        Set<ProcRole> usersProcRolesFrom = new HashSet<ProcRole>();
        
        for (CardProc cp : card.getProcs()) {
            if (cp.getProc().equals(procRoleTo.getProc())) {

//              BooleanUtils.isNotTrue(cp.getActive() - not-active process in case of cardProcFrame in Card Editor
//              BooleanUtils.isTrue(cp.getActive()) && StringUtils.isBlank(cp.getState()) - not-active process in case of cardRolesFrame in TransitionForm 
                if (BooleanUtils.isNotTrue(cp.getActive()) ||
                        (BooleanUtils.isTrue(cp.getActive()) && StringUtils.isBlank(cp.getState()))) {

                    state = WfConstants.PROC_NOT_ACTIVE;
                    //figure out  whether currentUser is card creator
                    User substitutedCreator = card.getSubstitutedCreator();
                    if (substitutedCreator == null) {
                        Transaction tx = Locator.createTransaction();
                        try {
                            EntityManager em = PersistenceProvider.getEntityManager();
                            Query query = em.createQuery("select c.substitutedCreator from wf$Card c where c.id = :card");
                            query.setParameter("card", card);
                            substitutedCreator = (User)query.getSingleResult();
                            tx.commit();
                        } finally {
                            tx.end();
                        }
                    }

                    //if current user is task creator
                    if (currentUser.equals(substitutedCreator)) {
                        //find proc in cahce
                        Proc currentProc = null;
                        for (Proc p : processes) {
                            if (procRoleTo.getProc().equals(p)) {
                                currentProc = p;
                                break;
                            }
                        }

                        //find CARD_CREATOR procRole for current process and add it to usersProcRolesFrom
                        for (ProcRole pr : currentProc.getRoles()) {
                            if (WfConstants.CARD_CREATOR.equals(pr.getCode())) {
                                usersProcRolesFrom.add(pr);
                                break;
                            }
                        }
                    }
                };
                break;
            }
        }

        //find procRoles which curent user has for current card
        for(CardRole cr : card.getRoles()) {
            if (cr.getUser().equals(currentUser)) {
                usersProcRolesFrom.add(cr.getProcRole());
            }
        }

        for (ProcRolePermission permission : permissions) {
            if (usersProcRolesFrom.contains(permission.getProcRoleFrom())
                    && StringUtils.equals(permission.getState(), state)
                    && permission.getProcRoleTo().equals(procRoleTo)
                    && permission.getType() == type
                    && permission.getValue() == ProcRolePermissionValue.ALLOW) {
                return true;
            }
        }

        return false;
    }

    public void clearPermissionsCache() {
        if (permissions != null)
            permissions = null;
    }

    private View getProcView() {
        return new View(Proc.class).addProperty("roles", new View(ProcRole.class).addProperty("code"));
    }
}
