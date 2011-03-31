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

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.workflow.core.DesignImportExportHelper;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

@Service(DesignerService.NAME)
public class DesignerServiceBean implements DesignerService {

    @Inject
    private DesignCompiler compiler;

    @Inject
    private DesignDeployer deployer;

    @Inject
    private ProcessMigrator migrator;

    public UUID copyDesign(UUID srcId) {
        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Design src = em.find(Design.class, srcId);

            Design design = new Design();
            String name = MessageProvider.getMessage(getClass(), "copyPrefix") + " " + src.getName();
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

            tx.commit();

            return design.getId();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            tx.end();
        }
    }

    public void compileDesign(UUID designId) throws DesignCompilationException {
        compiler.compileDesign(designId);
    }

    public void deployDesign(UUID designId, UUID procId) throws DesignDeploymentException {
        ProcessMigrator.Result result = null;
        if (procId != null) {
            result = migrator.checkMigrationPossibility(designId, procId);
            if (!result.isSuccess())
                throw new DesignDeploymentException(result.getMessage());
        }

        deployer.deployDesign(designId, procId);

        if (result != null && result.getOldJbpmProcessKey() != null) {
            migrator.migrate(designId, procId, result.getOldJbpmProcessKey());
        }
    }

    public Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages)
            throws DesignCompilationException {
        return compiler.compileMessagesForLocalization(design, languages);
    }

    public byte[] exportDesign(Design design) throws IOException, FileStorageException {
        return DesignImportExportHelper.exportDesign(design);
    }

    public Design importDesign(byte[] bytes) throws IOException, FileStorageException {
        return DesignImportExportHelper.importDesign(bytes);
    }

    public byte[] getNotificationMatrixTemplate(UUID designId) throws DesignCompilationException, TemplateGenerationException {
        return this.compiler.compileXlsTemplate(designId);
    }
}
