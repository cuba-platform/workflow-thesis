/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.12.10 14:48
 *
 * $Id$
 */
package com.haulmont.workflow.core.app.design;

import com.google.common.base.Preconditions;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.global.TimeProvider;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class DesignCompiler {

    private Map<String, String> moduleClassNames;

    private volatile Map<String, Class<? extends Module>> moduleClasses;

    protected FormCompiler formCompiler;

    private Log log = LogFactory.getLog(DesignCompiler.class);

    // set from spring.xml
    public void setModuleClasses(Map<String, String> moduleClassNames) {
        this.moduleClassNames = moduleClassNames;
    }

    private Map<String, Class<? extends Module>> getModuleClasses() {
        if (moduleClasses == null) {
            synchronized (this) {
                moduleClasses = new HashMap<String, Class<? extends Module>>();

                for (Map.Entry<String, String> entry : moduleClassNames.entrySet()) {
                    moduleClasses.put(entry.getKey(), ScriptingProvider.loadClass(entry.getValue()));
                }
            }
        }
        return moduleClasses;
    }

    // set from spring.xml
    public void setFormCompiler(FormCompiler formCompiler) {
        this.formCompiler = formCompiler;
    }

    public void compileDesign(UUID designId) throws DesignCompilationException {
        Preconditions.checkArgument(designId != null, "designId is null");
        log.info("Compiling design " + designId);

        Transaction tx = Locator.createTransaction();
        try {
            EntityManager em = PersistenceProvider.getEntityManager();
            Design design = em.find(Design.class, designId);

            List<Module> modules = new ArrayList<Module>();

            createModules(design, modules);

            cleanup(design);

            String jpdl = compileJpdl(modules);
            saveDesignFile(design, "", "jpdl", jpdl, null);

            Element localization = null;
            if (!StringUtils.isBlank(design.getLocalization())) {
                Document document = Dom4j.readDocument(design.getLocalization());
                localization = document.getRootElement();
            }

            String messagesEn = compileMessages(modules, "en", localization);
            saveDesignFile(design, "messages.properties", "messages", messagesEn, null);

            String messagesRu = compileMessages(modules, "ru", localization);
            saveDesignFile(design, "messages_ru.properties", "messages", messagesRu, null);

            String forms = compileForms(modules);
            saveDesignFile(design, "", "forms", forms, null);

            if (design.getNotificationMatrixUploaded() != null && design.getNotificationMatrix().length > 0)
                saveDesignFile(design, "", "notification", null, design.getNotificationMatrix());

            design.setCompileTs(TimeProvider.currentTimestamp());

            tx.commit();

            log.info("Design " + designId + " succesfully compiled");
        } catch (JSONException e) {
            throw new DesignCompilationException(e);
        } finally {
            tx.end();
        }
    }

    private void createModules(Design design, List<Module> modules) throws JSONException, DesignCompilationException {
        JSONObject json = new JSONObject(design.getSrc());

        JSONObject jsWorking = json.getJSONObject("working");

        JSONArray jsModules = jsWorking.getJSONArray("modules");
        for (int i = 0; i < jsModules.length(); i++) {
            JSONObject jsModule = jsModules.getJSONObject(i);
            String name = jsModule.getString("name");
            Class<? extends Module> moduleClass = getModuleClasses().get(name);
            if (moduleClass == null) {
                throw new RuntimeException("Unsupported module: " + name);
            }
            Module module;
            try {
                module = moduleClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            module.init(new Module.Context(design, jsModule, formCompiler));
            modules.add(module);
        }

        JSONArray jsWires = jsWorking.getJSONArray("wires");
        for (int i = 0; i < jsWires.length(); i++) {
            JSONObject jsWire = jsWires.getJSONObject(i);
            JSONObject jsWireSrc = jsWire.getJSONObject("src");
            JSONObject jsWireDst = jsWire.getJSONObject("tgt");

            Module srcModule = modules.get(jsWireSrc.getInt("moduleId"));
            Module dstModule = modules.get(jsWireDst.getInt("moduleId"));
            srcModule.addTransition(jsWireSrc.getString("terminal"), dstModule, jsWireDst.getString("terminal"));
        }
    }

    private void cleanup(Design design) {
        EntityManager em = PersistenceProvider.getEntityManager();

        Query q = em.createQuery("delete from wf$DesignFile df where df.design.id = ?1");
        q.setParameter(1, design.getId());
        q.executeUpdate();
    }

    private void saveDesignFile(Design design, String name, String type, String content, byte[] binaryContent) {
        EntityManager em = PersistenceProvider.getEntityManager();

        DesignFile df = new DesignFile();
        df.setDesign(design);
        df.setContent(content);
        df.setBinaryContent(binaryContent);
        df.setName(name);
        df.setType(type);
        em.persist(df);
    }

    private String compileJpdl(List<Module> modules) throws DesignCompilationException {
        Document document = DocumentHelper.createDocument();
        Element rootEl = document.addElement("process", "http://jbpm.org/4.2/jpdl");

        for (Module module : modules) {
            module.writeJpdlXml(rootEl);
        }

        Element onEl = rootEl.addElement("on");
        onEl.addAttribute("event", "end");
        Element listenerEl = onEl.addElement("event-listener");
        listenerEl.addAttribute("class", "workflow.activity.EndProcessListener");

        return Dom4j.writeDocument(document, true);
    }

    private String compileMessages(List<Module> modules, String lang, Element localization) {
        Properties properties = new Properties();

        Locale locale = new Locale(lang);
        String mp = "com.haulmont.workflow.core.app.design";

        compileMessage(properties, locale, "SAVE_ACTION", mp);
        compileMessage(properties, locale, "SAVE_AND_CLOSE_ACTION", mp);
        compileMessage(properties, locale, "START_PROCESS_ACTION", mp);
        compileMessage(properties, locale, "CANCEL_PROCESS_ACTION", mp);

        for (Module module : modules) {
            module.writeMessages(properties, lang);
        }

        L10nHelper lh = new L10nHelper(localization);
        for (String propName : properties.stringPropertyNames()) {
            String val = lh.get(lang, propName);
            if (val != null)
                properties.setProperty(propName, val);
        }

        StringWriter writer = new StringWriter();
        try {
            properties.store(writer, "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return writer.toString();
    }

    private void compileMessage(Properties properties, Locale locale, String key, String messagePack) {
        properties.setProperty(key, MessageProvider.getMessage(messagePack, key, locale));
    }

    private String compileForms(List<Module> modules) throws DesignCompilationException {
        Document document = DocumentHelper.createDocument();
        Element rootEl = document.addElement("forms", "http://www.haulmont.com/schema/cuba/workflow/forms.xsd");

        for (Module module : modules) {
            module.writeFormsXml(rootEl);
        }

        return Dom4j.writeDocument(document, true);
    }

    public Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages) throws DesignCompilationException {
        Map<String, Properties> result = new HashMap<String, Properties>();

        List<Module> modules = new ArrayList<Module>();
        try {
            createModules(design, modules);
        } catch (JSONException e) {
            throw new DesignCompilationException(e);
        }

        for (String lang : languages) {
            String messagesStr = compileMessages(modules, lang, null);
            Properties messages = new Properties();
            try {
                messages.load(new StringReader(messagesStr));
            } catch (IOException e) {
                throw new DesignCompilationException(e);
            }
            result.put(lang, messages);
        }

        return result;
    }

    private static class L10nHelper {

        private Element root;

        L10nHelper(Element root) {
            this.root = root;
        }

        String get(String lang, String key) {
            if (root == null)
                return null;

            String[] parts = key.split("\\.");
            Element element = root;
            for (int i = 0; i < parts.length; i++) {
                final String part = parts[i];
                for (Element el : Dom4j.elements(element, "key")) {
                    if (el.attributeValue("id").equals(part)) {
                        if (i < parts.length - 1) {
                            element = el;
                        } else {
                            for (Element valueEl : Dom4j.elements(el, "value")) {
                                if (lang.equals(valueEl.attributeValue("lang")) && !StringUtils.isEmpty(valueEl.getText()))
                                    return valueEl.getText();
                            }
                        }
                    }
                }
            }
            return null;
        }
    }
}
