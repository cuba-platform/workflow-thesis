/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 13:48
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.DesignImportExportHelper;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Service(DesignerService.NAME)
public class DesignerServiceBean implements DesignerService {

    @Inject
    private DesignCompiler compiler;

    @Inject
    private DesignDeployer deployer;

    @Inject
    private ProcessMigrator migrator;

    public UUID copyDesign(UUID srcId) {
        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
            Design src = em.find(Design.class, srcId);

            Design design = new Design();
            String name = AppBeans.get(Messages.class).getMessage(getClass(), "copyPrefix") + " " + src.getName();
            design.setName(name);

            if (!StringUtils.isBlank(src.getSrc())) {
                JSONObject json = new JSONObject(src.getSrc());
                json.put("name", name);

                JSONObject jsWorking = json.optJSONObject("working");
                if (jsWorking != null) {
                    JSONObject jsProperties = jsWorking.optJSONObject("properties");
                    jsProperties.put("name", name);
                }

                design.setSrc(json.toString());
            }

            design.setNotificationMatrix(src.getNotificationMatrix());
            design.setNotificationMatrixUploaded(src.getNotificationMatrixUploaded());
            design.setLocalization(src.getLocalization());
            em.persist(design);

            for (DesignScript srcScript : src.getScripts()) {
                DesignScript designScript = new DesignScript();
                designScript.setDesign(design);
                designScript.setName(srcScript.getName());
                designScript.setContent(srcScript.getContent());
                em.persist(designScript);
            }

            for (DesignProcessVariable variable : src.getDesignProcessVariables()) {
                DesignProcessVariable processVariable = new DesignProcessVariable();
                processVariable.setDesign(design);
                processVariable.setShouldBeOverridden(variable.getShouldBeOverridden());
                processVariable = (DesignProcessVariable) variable.copyTo(processVariable);
                em.persist(processVariable);
            }

            tx.commit();

            return design.getId();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            tx.end();
        }
    }

    public CompilationMessage compileDesign(UUID designId) throws DesignCompilationException {
        return compiler.compileDesign(designId);
    }

    public void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException {
        ProcessMigrator.Result result = null;
        if (procId != null) {
            result = migrator.checkMigrationPossibility(designId, procId);
            if (!result.isSuccess())
                throw new DesignDeploymentException(result.getMessage());
        }

        deployer.deployDesign(designId, procId, role);

        if (result != null && result.getOldJbpmProcessKey() != null) {
            migrator.migrate(designId, procId, result.getOldJbpmProcessKey());
        }
    }

    public Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages)
            throws DesignCompilationException {
        return compiler.compileMessagesForLocalization(design, languages);
    }

    @Override
    public byte[] exportDesign(Design design) throws IOException, FileStorageException {
        return DesignImportExportHelper.exportDesigns(Arrays.asList(design));
    }

    @Override
    public Design importDesign(byte[] bytes) throws IOException, FileStorageException {
        return DesignImportExportHelper.importDesigns(bytes).iterator().next();
    }

    public byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException {
        return DesignImportExportHelper.exportDesigns(designs);
    }

    public Collection<Design> importDesigns(byte[] bytes) throws IOException, FileStorageException {
        return DesignImportExportHelper.importDesigns(bytes);
    }

    public byte[] getNotificationMatrixTemplate(UUID designId) throws TemplateGenerationException {
        return this.compiler.compileXlsTemplate(designId);
    }

    public void saveNotificationMatrixFile(Design design) {
        if (BooleanUtils.isTrue(design.getNotificationMatrixUploaded()) &&
                (design.getNotificationMatrix().length > 0)) {
            Transaction tx = AppBeans.get(Persistence.class).createTransaction();
            try {
                EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
                //Delete previos notifications file
                Query query = em.createQuery();
                query.setQueryString("delete from wf$DesignFile df where df.type='notification' and df.design.id=:design");
                query.setParameter("design", design);
                query.executeUpdate();

                DesignFile df = new DesignFile();
                df.setDesign(design);
                df.setContent(null);
                df.setBinaryContent(design.getNotificationMatrix());
                df.setName("");
                df.setType("notification");
                em.persist(df);

                tx.commit();
            } finally {
                tx.end();
            }
        }
    }
}
