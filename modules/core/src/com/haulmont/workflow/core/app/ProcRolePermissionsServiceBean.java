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
import com.haulmont.cuba.core.global.MetadataProvider;
import com.haulmont.cuba.core.global.PersistenceHelper;
import com.haulmont.cuba.core.global.View;
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

import javax.annotation.ManagedBean;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service(ProcRolePermissionsService.NAME)
public class ProcRolePermissionsServiceBean implements ProcRolePermissionsService {

    private Log log = LogFactory.getLog(ProcRolePermissionsServiceBean.class);

    private List<ProcRolePermission> permissions;

    private void init() {
        log.debug("initializing ProcRolePermissionService");
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Query query = em.createQuery("select prp from wf$ProcRolePermission prp");
            query.setView(MetadataProvider.getViewRepository().getView(ProcRolePermission.class, "edit"));
            permissions = query.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
    }

    public boolean isPermitted(CardRole cardRoleTo, String state, ProcRolePermissionType type) {
        //for not-perisisted entitites we'll allow everything
        if (PersistenceHelper.isNew(cardRoleTo)) return true;
        return isPermitted(cardRoleTo.getCard(), cardRoleTo.getProcRole(), state, type);
    }

    public boolean isPermitted(Card card, ProcRole procRoleTo, String state, ProcRolePermissionType type) {
        if (BooleanUtils.isNotTrue(procRoleTo.getProc().getPermissionsEnabled())) return true;

        //for not-active processes we'll allow everything by now
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
        for (CardProc cp : card.getProcs()) {
            if (cp.getProc().equals(procRoleTo.getProc())) {
                if (BooleanUtils.isNotTrue(cp.getActive())) return true;
                break;
            }
        }

        if (permissions == null) init();

        //find procRoles which curent user has for current card
        User currentUser = SecurityProvider.currentUserSession().getCurrentOrSubstitutedUser();
        Set<ProcRole> usersProcRolesFrom = new HashSet<ProcRole>();
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

    public void clearPermissionsCahche() {
        if (permissions != null)
            permissions.clear();
    }
}
