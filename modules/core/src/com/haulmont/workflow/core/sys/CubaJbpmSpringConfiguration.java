/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.sys;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.ConfigurationResourceLoader;
import com.haulmont.cuba.core.sys.persistence.DbmsType;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.jbpm.pvm.internal.cfg.SpringConfiguration;
import org.springframework.core.io.Resource;

import java.io.*;

/**
 * Configures jBPM depending on {@link DbmsType}.
 *
 * <p>$Id$</p>
 *
 * @author krivopustov
 */
public class CubaJbpmSpringConfiguration extends SpringConfiguration {

    public CubaJbpmSpringConfiguration() {
        super();

        String dataDir = AppContext.getProperty("cuba.dataDir");
        File hibernateCfgFile = new File(dataDir, "jbpm.hibernate.cfg.xml");
        File jbpmCfgFile = new File(dataDir, "jbpm.cfg.xml");

        String hibernateDialect;
        switch (DbmsType.getCurrent()) {
            case HSQL:
                hibernateDialect = "org.hibernate.dialect.HSQLDialect";
                break;
            case POSTGRES:
                hibernateDialect = "org.hibernate.dialect.PostgreSQLDialect";
                break;
            case MSSQL:
                hibernateDialect = "org.hibernate.dialect.SQLServerDialect";
                break;
            default:
                throw new UnsupportedOperationException("Unknown DBMS type: " + DbmsType.getCurrent());
        }

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

        resource = new ConfigurationResourceLoader().getResource("wf.jbpm.cfg.xml");
        outputStream = null;
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

        try {
            setInputStream(new FileInputStream(jbpmCfgFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
