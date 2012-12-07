/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 07.06.2010 17:28:27
 *
 * $Id$
 */
package com.haulmont.workflow.core.permissions;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Resources;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.app.Authenticated;
import com.haulmont.workflow.core.entity.Proc;
import com.haulmont.workflow.core.entity.ProcRole;
import com.haulmont.workflow.core.entity.ProcRolePermission;
import com.haulmont.workflow.core.global.ProcRolePermissionType;
import com.haulmont.workflow.core.global.ProcRolePermissionValue;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.ManagedBean;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

@ManagedBean(PermissionsManagerMBean.NAME)
public class PermissionsManager implements PermissionsManagerMBean {

    @Inject
    private Resources resources;

    @Inject
    private Persistence persistence;

    @Authenticated
    @Override
    public String deployPermissions(String procName) {
        procName = procName.trim();
        String filePath = "/process/" + procName + "/permissions.xml";

        Transaction tx = persistence.createTransaction();
        try {
            String xml = resources.getResourceAsString(filePath);
            if (xml == null) {
                return "File " + filePath + " not found";
            }
            
            EntityManager em = persistence.getEntityManager();
            em.setView(getProcView());
            Query query = em.createQuery("select p from wf$Proc p where p.jbpmProcessKey = ?1");
            query.setParameter(1, StringUtils.capitalize(procName));
            Proc proc = (Proc) query.getSingleResult();
            if (proc == null) return "Process with name " + procName + " not found";

            Map<String, ProcRole> procRoles = new HashMap<String, ProcRole>();
            for (ProcRole pr : proc.getRoles()) {
                procRoles.put(pr.getCode(), pr);
            }

            query = em.createQuery("delete from wf$ProcRolePermission prp where prp.procRoleFrom.proc.id = :proc");
            query.setParameter("proc", proc);
            query.executeUpdate();

            Document doc = Dom4j.readDocument(xml);
            Element root = doc.getRootElement();
            for (Element whoChangesElement : Dom4j.elements(root)) {
                String whoChanges = whoChangesElement.attributeValue("name");
                ProcRole whoChangesProcRole = procRoles.get(whoChanges);
                if (whoChangesProcRole == null)
                    return "ProcRole with name " + whoChanges + " doesn't exists";
                for (Element permission : Dom4j.elements(whoChangesElement, "permission")) {
                    String whomChanges = permission.attributeValue("whomChanges");
                    String state = permission.attributeValue("state");
                    String type = permission.attributeValue("type");
                    String value = permission.attributeValue("value");

                    ProcRole whomChangesProcRole = procRoles.get(whomChanges);
                    if (whomChangesProcRole == null)
                        return "ProcRole with name " + whoChanges + " doesn't exists";

                    ProcRolePermissionValue procRolePermissionValue = ProcRolePermissionValue.fromString(value);
                    if ("ALL".equals(type)) {
                        for (int i = 1; i <=3; i++) {
                            ProcRolePermissionType procRolePermissionType = ProcRolePermissionType.fromId(i);
                            ProcRolePermission procRolePermission = new ProcRolePermission();
                            procRolePermission.setProcRoleFrom(whoChangesProcRole);
                            procRolePermission.setProcRoleTo(whomChangesProcRole);
                            procRolePermission.setState(state);
                            procRolePermission.setValue(procRolePermissionValue);
                            procRolePermission.setType(procRolePermissionType);
                            em.persist(procRolePermission);
                        }
                    } else {
                        ProcRolePermissionType procRolePermissionType = ProcRolePermissionType.fromString(type);
                        ProcRolePermission procRolePermission = new ProcRolePermission();
                        procRolePermission.setProcRoleFrom(whoChangesProcRole);
                        procRolePermission.setProcRoleTo(whomChangesProcRole);
                        procRolePermission.setState(state);
                        procRolePermission.setValue(procRolePermissionValue);
                        procRolePermission.setType(procRolePermissionType);
                        em.persist(procRolePermission);
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            return ExceptionUtils.getStackTrace(e);
        } finally {
            tx.end();
        }
        return "Permissions successfully deployed";
    }

    private View getProcView() {
        return new View(Proc.class)
                .addProperty("jbpmProcessKey")
                .addProperty("roles", new View(ProcRole.class).addProperty("code"));
    }
}
