/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.ConfigurationResourceLoader;
import com.haulmont.cuba.core.sys.persistence.DbmsType;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.api.ProcessEngine;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.processengine.SpringHelper;
import org.springframework.core.io.Resource;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Configures jBPM depending on {@link DbmsType}.
 *
 */
public class CubaJbpmSpringHelper extends SpringHelper {

    private String jbpmConfiguration;
    private String hibernateConfiguration;

    public CubaJbpmSpringHelper() {
        super();

        String dataDir = AppContext.getProperty("cuba.dataDir");
        File hibernateCfgFile = modifyHibernateCfgXml(dataDir, getHibernateDialectName());
        File jbpmCfgFile = modifyJbpmCfgXml(dataDir, hibernateCfgFile);

        jbpmConfiguration = jbpmCfgFile.toURI().toString();
        hibernateConfiguration = hibernateCfgFile.toURI().toString();
    }

    protected String getHibernateDialectName() {
        switch (DbmsType.getType()) {
            case "hsql":
                return "org.hibernate.dialect.HSQLDialect";
            case "postgres":
                return "org.hibernate.dialect.PostgreSQLDialect";
            case "mssql":
                return "org.hibernate.dialect.SQLServerDialect";
            case "oracle":
                return "org.hibernate.dialect.Oracle10gDialect";
            case "mysql":
                return "org.hibernate.dialect.MySQLDialect";
            default:
                throw new UnsupportedOperationException("Unknown DBMS type: " + DbmsType.getType());
        }
    }

    @Override
    public ProcessEngine createProcessEngine() {
        try {
            processEngine = new ConfigurationImpl()
                    .springInitiated(applicationContext)
                    .setInputStream(new FileInputStream(new File(new URI(getJbpmConfiguration()))))
                    .buildProcessEngine();
            return processEngine;
        } catch (FileNotFoundException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJbpmConfiguration() {
        return jbpmConfiguration;
    }

    public String getHibernateConfiguration() {
        return hibernateConfiguration;
    }

    @SuppressWarnings("unchecked")
    private File modifyHibernateCfgXml(String dataDir, String hibernateDialect) {
        File hibernateCfgFile = new File(dataDir, "jbpm.hibernate.cfg.xml");
        Resource resource = new ConfigurationResourceLoader().getResource("wf.jbpm.hibernate.cfg.xml");
        FileOutputStream outputStream = null;
        try {
            Document doc = Dom4j.readDocument(resource.getInputStream());
            Element factoryEl = doc.getRootElement().element("session-factory");
            for (Element propertyEl : Dom4j.elements(factoryEl, "property")) {
                if (propertyEl.attributeValue("name").equals("hibernate.dialect")) {
                    propertyEl.setText(hibernateDialect);
                }
            }
            outputStream = new FileOutputStream(hibernateCfgFile);
            Dom4j.writeDocument(doc, true, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        // Insert DTD because Hibernate validates XML on load
        try {
            List<String> lines = FileUtils.readLines(hibernateCfgFile, StandardCharsets.UTF_8);

            lines.add(1, "<!DOCTYPE hibernate-configuration PUBLIC" +
                    " \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\"" +
                    " \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">");

            FileUtils.writeLines(hibernateCfgFile, StandardCharsets.UTF_8.name(), lines, "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hibernateCfgFile;
    }

    private File modifyJbpmCfgXml(String dataDir, File hibernateCfgFile) {
        File jbpmCfgFile = new File(dataDir, "jbpm.cfg.xml");
        Resource resource = new ConfigurationResourceLoader().getResource("wf.jbpm.cfg.xml");
        FileOutputStream outputStream = null;
        try {
            Document doc = Dom4j.readDocument(resource.getInputStream());
            Element pecEl = doc.getRootElement().element("process-engine-context");
            Element hibCfgEl = pecEl.element("hibernate-configuration");
            if (hibCfgEl == null)
                hibCfgEl = pecEl.addElement("hibernate-configuration");
            hibCfgEl.addElement("cfg").addAttribute("file", hibernateCfgFile.getAbsolutePath());
            outputStream = new FileOutputStream(jbpmCfgFile);
            Dom4j.writeDocument(doc, true, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
        return jbpmCfgFile;
    }
}