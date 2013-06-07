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
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.security.entity.Role;
import com.haulmont.workflow.core.app.WfEngineAPI;
import com.haulmont.workflow.core.entity.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.ExternalizableConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

public class DesignDeployer {

    public static final String SCRIPTS_DIR = "scripts";

    private Log log = LogFactory.getLog(DesignDeployer.class);

    public void deployDesign(UUID designId, UUID procId, Role role) {
        Preconditions.checkArgument(designId != null, "designId is null");

        log.info("Deploying design " + designId + " into process " + procId);

        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
            Design design = em.find(Design.class, designId);
            if (design.getCompileTs() == null)
                throw new IllegalStateException("Design is not compiled");

            String procKey = "proc_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(AppBeans.get(TimeSource.class).currentTimestamp());

            Proc proc = procId != null ? em.find(Proc.class, procId) : null;

            File dir = new File(AppBeans.get(Configuration.class).getConfig(GlobalConfig.class).getConfDir(), "process/" + procKey);
            if (dir.exists()) {
                backupExisting(dir);
            }

            if (!dir.mkdirs())
                throw new RuntimeException("Unable to create directory " + dir.getAbsolutePath());

            List<DesignFile> designFiles = em.createQuery("select df from wf$DesignFile df where df.design.id = ?1")
                    .setParameter(1, designId)
                    .getResultList();

            proc = deployJpdl(design, designFiles, procKey, proc, dir);

            if (role!=null)
                proc.setAvailableRole(role);

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

    private Proc deployJpdl(Design design, List<DesignFile> designFiles, String procKey, Proc proc, File dir) throws IOException {
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
        proc = wfEngine.deployJpdlXml("/process/" + procKey + "/" + jpdlFile.getName(), proc);

        proc.setDesign(design);
        if (proc.getName().equals(proc.getJbpmProcessKey())) {
            proc.setName(design.getName());
        }
        return proc;
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
        List<DesignScript> designScripts = AppBeans.get(Persistence.class).getEntityManager().createQuery(
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

    private static String toXML(Object o) {
        XStream xStream = createXStream(o.getClass());
        return xStream.toXML(o);
    }

    private static XStream createXStream(Class clazz) {
        XStream xStream = new XStream();
        xStream.getConverterRegistry().removeConverter(ExternalizableConverter.class);
        xStream.alias(clazz.getSimpleName(), clazz);
        for (Field field : clazz.getDeclaredFields()) {
            Class cl = field.getType();
            xStream.alias(cl.getSimpleName(), cl);
        }
        return xStream;
    }

    private void deployProcessVariables(Proc proc, Design design, File dir) throws IOException {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        List<DesignProcessVariable> designProcessVariables = AppBeans.get(Persistence.class).getEntityManager().createQuery(
                "select s from wf$DesignProcessVariable s where s.design.id = ?1")
                .setParameter(1, design.getId())
                .setView(MetadataProvider.getViewRepository().getView(DesignProcessVariable.class, View.LOCAL))
                .getResultList();

        List<ProcVariable> existsProcVariables = AppBeans.get(Persistence.class).getEntityManager().createQuery(
                "select s from wf$ProcVariable s where s.proc.id = ?1")
                .setParameter(1, proc.getId())
                .setView(AppBeans.get(Metadata.class).getViewRepository().getView(ProcVariable.class, View.LOCAL))
                .getResultList();

        List<ProcVariable> procVariables = new ArrayList<ProcVariable>();

        Map<String, DesignProcessVariable> designProcessVariableMap = new HashMap<String, DesignProcessVariable>();
        for (DesignProcessVariable designProcessVariable : designProcessVariables) {
            designProcessVariableMap.put(designProcessVariable.getAlias(), designProcessVariable);
        }

        for (ProcVariable procVariable : existsProcVariables) {
            DesignProcessVariable existDesignProcessVariable = designProcessVariableMap.get(procVariable.getAlias());
            if (existDesignProcessVariable != null) {
                if (BooleanUtils.isNotTrue(procVariable.getOverridden())) {
                    procVariable.setValue(existDesignProcessVariable.getValue());
                }
                procVariables.add(em.merge(procVariable));
                designProcessVariables.remove(existDesignProcessVariable);
            }
        }

        for (DesignProcessVariable designProcessVariable : designProcessVariables) {
            ProcVariable procVariable = (ProcVariable) designProcessVariable.copyTo(new ProcVariable());
            procVariable.setProc(proc);
            em.persist(procVariable);
            procVariables.add(procVariable);
        }

        File file = new File(dir, "variables.xml");
        FileUtils.writeStringToFile(file, toXML(procVariables), "UTF-8");
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
        String tmpDir = AppBeans.get(Configuration.class).getConfig(GlobalConfig.class).getTempDir();
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
