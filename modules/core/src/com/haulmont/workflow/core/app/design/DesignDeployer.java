/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 29.12.10 12:11
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import com.google.common.base.Preconditions;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.ConfigProvider;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignScript;
import com.haulmont.workflow.core.entity.Proc;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class DesignDeployer {

    public static final String SCRIPTS_DIR = "scripts";

    private Log log = LogFactory.getLog(DesignDeployer.class);

    public void deployDesign(UUID designId, UUID procId) {
        Preconditions.checkArgument(designId != null, "designId is null");

        log.info("Deploying design " + designId + " into process " + procId);

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Design design = em.find(Design.class, designId);
            if (design.getCompileTs() == null)
                throw new IllegalStateException("Design is not compiled");

            String procKey = "proc_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(TimeProvider.currentTimestamp());

            Proc proc = procId != null ? em.find(Proc.class, procId) : null;

            File dir = new File(ConfigProvider.getConfig(GlobalConfig.class).getConfDir(), "process/" + procKey);
            if (dir.exists()) {
                backupExisting(dir);
            }

            if (!dir.mkdirs())
                throw new RuntimeException("Unable to create directory " + dir.getAbsolutePath());

            List<DesignFile> designFiles = em.createQuery("select df from wf$DesignFile df where df.design.id = ?1")
                    .setParameter(1, designId)
                    .getResultList();

            deployJpdl(design, designFiles, procKey, proc, dir);

            deployMessages(designFiles, dir);

            deployForms(designFiles, dir);

            deployScripts(design, dir);

            deployNotificationMatrix(designFiles, dir);

            tx.commit();

            log.info("Design " + designId + " deployed succesfully");
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            tx.end();
        }
    }

    private void deployJpdl(Design design, List<DesignFile> designFiles, String procKey, Proc proc, File dir) throws IOException {
        DesignFile jpdlDf = null;
        for (DesignFile df : designFiles) {
            if (df.getType().equals("jpdl")) {
                jpdlDf = df;
                break;
            }
        }
        if (jpdlDf == null)
            throw new IllegalStateException("JPDL not found");

        File jpdlFile;

        jpdlFile = new File(dir, procKey + ".jpdl.xml");

        Document document = Dom4j.readDocument(jpdlDf.getContent());
        document.getRootElement().addAttribute("name", procKey);
        document.getRootElement().addAttribute("key", procKey);

        FileUtils.writeStringToFile(jpdlFile, Dom4j.writeDocument(document, true), "UTF-8");

        WfEngineAPI wfEngine = Locator.lookup(WfEngineAPI.NAME);
        proc = wfEngine.deployJpdlXml(jpdlFile.getAbsolutePath(), proc);

        proc.setDesign(design);
        if (proc.getName().equals(proc.getJbpmProcessKey())) {
            proc.setName(design.getName());
        }
    }

    private void deployMessages(List<DesignFile> designFiles, File dir) throws IOException {
        for (DesignFile df : designFiles) {
            if (df.getType().equals("messages")) {
                File file = new File(dir, df.getName());
                FileUtils.writeStringToFile(file, df.getContent(), "UTF-8");
            }
        }
    }

    private void deployForms(List<DesignFile> designFiles, File dir) throws IOException {
        for (DesignFile df : designFiles) {
            if (df.getType().equals("forms")) {
                File file = new File(dir, "forms.xml");
                FileUtils.writeStringToFile(file, df.getContent(), "UTF-8");
                break;
            }
        }
    }

    private void deployScripts(Design design, File dir) throws IOException {
        List<DesignScript> designScripts = PersistenceProvider.getEntityManager().createQuery(
                "select s from wf$DesignScript s where s.design.id = ?1")
                .setParameter(1, design.getId())
                .getResultList();

        File scriptsDir = new File(dir, SCRIPTS_DIR);
        if (!scriptsDir.exists())
            dir.mkdir();

        for (DesignScript designScript : designScripts) {
            String script = "// " + designScript.getName() + "\n" + designScript.getContent();
            File file = new File(scriptsDir, designScript.getFileName());
            FileUtils.writeStringToFile(file, script, "UTF-8");
        }
    }

    private void deployNotificationMatrix(List<DesignFile> designFiles, File dir) throws IOException {
        for (DesignFile df : designFiles) {
            if (df.getType().equals("notification")) {
                File file = new File(dir, "notification.xls");
                FileUtils.writeByteArrayToFile(file, df.getBinaryContent());
                break;
            }
        }
    }

    private void backupExisting(File dir) throws IOException {
        String tmpDir = ConfigProvider.getConfig(GlobalConfig.class).getTempDir();
        File backupDir = new File(tmpDir, dir.getName() + ".backup");
        int i = 0;
        while (backupDir.exists()) {
            i++;
            backupDir = new File(tmpDir, dir.getName() + ".backup" + i);
        }
        FileUtils.moveDirectory(dir, backupDir);
        log.info("Directory " + dir.getAbsolutePath() + " moved to " + backupDir.getAbsolutePath());
    }
}
