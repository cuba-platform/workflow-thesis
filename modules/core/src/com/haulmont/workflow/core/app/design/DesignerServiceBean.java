/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.FileStorageException;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.DesignDeploymentException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;

@Service(DesignerService.NAME)
public class DesignerServiceBean implements DesignerService {

    @Inject
    protected DesignCompiler compiler;

    @Inject
    protected DesignerWorkerAPI designerWorkerAPI;

    @Override
    public UUID copyDesign(UUID srcId) {
        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
            Design src = em.find(Design.class, srcId);

            Metadata metadata = AppBeans.get(Metadata.NAME);

            Design design = metadata.create(Design.class);
            String name = AppBeans.get(Messages.class).getMessage(getClass(), "copyPrefix") + " " + src.getName();
            design.setName(name);
            design.setType(src.getType());

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
                DesignScript designScript = metadata.create(DesignScript.class);
                designScript.setDesign(design);
                designScript.setName(srcScript.getName());
                designScript.setContent(srcScript.getContent());
                em.persist(designScript);
            }

            if (design.getDesignProcessVariables() != null) {
                for (DesignProcessVariable variable : src.getDesignProcessVariables()) {
                    DesignProcessVariable processVariable = metadata.create(DesignProcessVariable.class);
                    processVariable.setDesign(design);
                    processVariable.setShouldBeOverridden(variable.getShouldBeOverridden());
                    processVariable = (DesignProcessVariable) variable.copyTo(processVariable);
                    em.persist(processVariable);
                }
            }

            tx.commit();

            return design.getId();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            tx.end();
        }
    }

    @Override
    public CompilationMessage compileDesign(UUID designId) throws DesignCompilationException {
        return designerWorkerAPI.compileDesign(designId);
    }

    @Override
    public void deployDesign(UUID designId, UUID procId, Role role) throws DesignDeploymentException {
        designerWorkerAPI.deployDesign(designId, procId, role);
    }

    @Override
    public Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages)
            throws DesignCompilationException {
        return compiler.compileMessagesForLocalization(design, languages);
    }

    @Override
    public byte[] exportDesign(Design design) throws IOException, FileStorageException {
        return designerWorkerAPI.exportDesign(design);
    }

    @Override
    public Design importDesign(byte[] bytes) throws IOException, FileStorageException {
        return designerWorkerAPI.importDesign(bytes);
    }

    @Override
    public byte[] exportDesigns(Collection<Design> designs) throws IOException, FileStorageException {
        return designerWorkerAPI.exportDesigns(designs);
    }

    @Override
    public Collection<Design> importDesigns(byte[] bytes) throws IOException, FileStorageException {
        return designerWorkerAPI.importDesigns(bytes);
    }

    @Override
    public byte[] getNotificationMatrixTemplate(UUID designId) throws TemplateGenerationException {
        return this.compiler.compileXlsTemplate(designId);
    }

    @Override
    public void saveNotificationMatrixFile(Design design) {
        if (BooleanUtils.isTrue(design.getNotificationMatrixUploaded()) &&
                (design.getNotificationMatrix().length > 0)) {
            Transaction tx = AppBeans.get(Persistence.class).createTransaction();
            try {
                EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
                //Delete previous notifications file
                Query query = em.createQuery();
                query.setQueryString("delete from wf$DesignFile df where df.type='notification' and df.design.id=:design");
                query.setParameter("design", design);
                query.executeUpdate();

                Metadata metadata = AppBeans.get(Metadata.NAME);

                DesignFile df = metadata.create(DesignFile.class);
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