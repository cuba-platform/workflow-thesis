/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.design.modules;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.*;
import com.haulmont.workflow.core.app.ProcessVariableAPI;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.Module;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.workflow.core.global.WfConstants.CARD_VARIABLES_SEPARATOR;
import static com.haulmont.workflow.core.global.WfConstants.SUBDESIGN_SEPARATOR;

/**
 * <p>$Id$</p>
 *
 * @author Zaharchenko
 */
public class SubDesignModule extends Module {

    protected JSONObject jsOptions;
    protected String subDesignId;
    protected String params;

    protected Map<String, String> transitionsMap;
    protected Design design;

    protected Messages messages = AppBeans.get(Messages.class);

    protected ProcessVariableAPI processVariableAPI = AppBeans.get(ProcessVariableAPI.NAME);

    public static final String SUBDESIGN_ELEMENT_NAME = "subdesign";
    protected Pattern variablePattern = Pattern.compile("^\\$\\{([a-zA-Z0-9]*)\\}.*$");

    public void init(Module.Context context) throws DesignCompilationException {
        super.init(context);
        Transaction tx = AppBeans.get(Persistence.class).getTransaction();
        try {
            jsOptions = jsValue.optJSONObject("options");
            subDesignId = jsOptions.getString("design");
            if (StringUtils.isEmpty(subDesignId)) {
                throw new DesignCompilationException(messages.formatMessage(AssignmentModule.class,
                        "exception.subdesignIsEmpty", StringEscapeUtils.escapeHtml(caption)));
            }
            checkDesignExist(subDesignId);
            params = jsValue.optString("params");
            tx.commit();
        } catch (JSONException e) {
            throw new DesignCompilationException(e);

        } finally {
            tx.end();
        }
        if (StringUtils.isEmpty(subDesignId)) {
            throw new DesignCompilationException(messages.formatMessage(getClass(),
                    "exception.noSubDesign", StringEscapeUtils.escapeHtml(caption)));
        }
    }

    public Element writeJpdlXml(Element parentEl) throws DesignCompilationException {
        DesignFile designFile = getDesignFile("jpdl", "");
        Document document = Dom4j.readDocument(designFile.getContent());
        Element subDesign = document.getRootElement();
        processSubDesignJpdl(parentEl, subDesign);
        addSubDesignElement(parentEl, getAttributeWithPrefix(subDesign.element("start").element("transition"), "to"));
        return parentEl;
    }

    protected void processSubDesignJpdl(Element parentEl, Element subDesign) {
        for (Element node : (List<Element>) subDesign.elements("custom")) {
            parentEl.elements().add(processNode(node));
        }
    }

    @Override
    public void writeFormsXml(Element rootEl) throws DesignCompilationException {
        DesignFile designFile = getDesignFile("forms", "");
        Document document = Dom4j.readDocument(designFile.getContent());
        Element subDesign = document.getRootElement();
        processSubDesignXml(rootEl, subDesign);
    }

    protected void processSubDesignXml(Element parentEl, Element subDesign) {
        for (Element node : (List<Element>) subDesign.elements("activity")) {
            parentEl.elements().add(processNode(node));
        }
    }

    @Override
    public void writeMessages(Properties properties, String lang) {
        super.writeMessages(properties, lang);

        List<String> propertiesForExclude = Arrays.asList("SAVE_ACTION",
                "SAVE_AND_CLOSE_ACTION", "START_PROCESS_ACTION", "CANCEL_PROCESS_ACTION", "Canceled");

        String fileName = "messages";
        if (!lang.equals("en")) {
            fileName = fileName + "_" + lang;
        }
        fileName = fileName + ".properties";
        try {
            DesignFile messagesFile = getDesignFile("messages", fileName);
            Properties subProcProperties = new Properties();
            subProcProperties.load(new StringReader(messagesFile.getContent()));
            for (String key : subProcProperties.stringPropertyNames()) {
                if (!propertiesForExclude.contains(key)) {
                    properties.setProperty(name + SUBDESIGN_SEPARATOR + key, subProcProperties.getProperty(key));
                }
            }
        } catch (DesignCompilationException | IOException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    protected DesignFile getDesignFile(String type, String fileName) throws DesignCompilationException {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        Query query = em.createQuery("select df from wf$DesignFile df where df.design.id = :subDesignId and df.type=:type and df.name = :fileName");
        query.setParameter("type", type);
        query.setParameter("fileName", fileName);
        query.setParameter("subDesignId", UUID.fromString(subDesignId));
        query.setView(AppBeans.get(Metadata.class).getViewRepository().getView(DesignFile.class, View.LOCAL));
        query.setMaxResults(1);
        List<DesignFile> designFiles = query.getResultList();
        if (designFiles.isEmpty()) {
            throw new DesignCompilationException(String.format(messages.getMessage(SubDesignModule.class,
                    "exception.noSubDesign"), StringEscapeUtils.escapeHtml(caption)));
        }
        return designFiles.get(0);
    }

    protected void addSubDesignElement(Element parentEl, String startTransitionName) {
        Element propEl = parentEl.addElement(SUBDESIGN_ELEMENT_NAME);
        propEl.addAttribute("name", name);
        propEl.addAttribute("subDesignId", subDesignId);
        propEl.addAttribute("startTransitionName", startTransitionName);
    }

    protected Element processNode(Element node) {
        node.detach();
        node.addAttribute("name", getAttributeWithPrefix(node, "name"));
        for (Element transition : (List<Element>) node.elements("transition")) {
            String destination = transition.attributeValue("to");
            String newDestination = getTransitionsMap().get(destination);
            if (newDestination != null) {
                transition.addAttribute("to", newDestination);
            } else {
                transition.addAttribute("to", getAttributeWithPrefix(transition, "to"));
            }
        }
        return node;
    }

    public static Map<String, String> parseParamsString(String paramsStr) throws DesignCompilationException {
        Map<String, String> params = new HashMap<String, String>();
        if (StringUtils.isEmpty(paramsStr)) {
            return params;
        }

        String[] keyValues = StringUtils.split(paramsStr, ",");
        for (String keyValue : keyValues) {
            String[] keyValueArray = keyValue.split(":");
            if (keyValueArray.length != 2) {
                throw new DesignCompilationException("Invalid parameters string : " + paramsStr);
            }
            String key = StringUtils.trimToEmpty(keyValueArray[0]);
            String value = StringUtils.trimToEmpty(keyValueArray[1]);
            params.put(key, value);
        }
        return params;

    }

    @Override
    public List<DesignProcessVariable> generateDesignProcessVariables() throws DesignCompilationException {
        super.generateDesignProcessVariables();
        if (design == null) {
            throw new DesignCompilationException(String.format(messages.getMessage(SubDesignModule.class,
                    "exception.noSubDesign"), StringEscapeUtils.escapeHtml(caption)));
        }
        Map<String, String> paramsMap = parseParamsString(params);
        for (DesignProcessVariable designProcessVariable : design.getDesignProcessVariables()) {
            DesignProcessVariable newDesignParameter = (DesignProcessVariable) designProcessVariable.copyTo(new DesignProcessVariable());
            newDesignParameter.setModuleName(prepareModuleNames(designProcessVariable.getModuleName()));
            newDesignParameter.setAlias(designProcessVariable.getAlias());
            if (paramsMap.containsKey(designProcessVariable.getAlias())) {
                String value = paramsMap.get(designProcessVariable.getAlias());
                Matcher matcher = variablePattern.matcher(value);
                if (matcher.find()) {
                    String alias = matcher.group(1);
                    newDesignParameter.setAlias(alias);
                } else {
                    newDesignParameter.setValue(value);
                    newDesignParameter.setAlias(name + "_" + designProcessVariable.getAlias());
                    try {
                        Object varValue = processVariableAPI.getValue(newDesignParameter);
                    } catch (IllegalStateException e) {
                        throw new DesignCompilationException(String.format(messages.getMessage(SubDesignModule.class, "incorrectValueInSubprocessParams"), value, designProcessVariable.getAlias(), MessageProvider.getMessage(designProcessVariable.getAttributeType())));
                    }
                }
            }
            newDesignParameter.setShouldBeOverridden(designProcessVariable.getShouldBeOverridden());
            designProcessVariables.add(newDesignParameter);
        }
        return designProcessVariables;
    }

    private String prepareModuleNames(String moduleNames) {
        if (StringUtils.isBlank(moduleNames)) return moduleNames;
        String[] modules = moduleNames.split(CARD_VARIABLES_SEPARATOR);
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(SUBDESIGN_SEPARATOR).append(modules[0]);
        for (int i = 1; i < modules.length; i++) {
            sb.append(CARD_VARIABLES_SEPARATOR).append(name).append(SUBDESIGN_SEPARATOR).append(modules[i]);
        }
        return sb.toString();
    }

    protected String getAttributeWithPrefix(Element node, String attribute) {
        return name + SUBDESIGN_SEPARATOR + node.attributeValue(attribute);
    }

    private Map<String, String> getTransitionsMap() {
        if (transitionsMap == null) {
            transitionsMap = new HashMap<String, String>();
            for (Module.Transition transition : transitions) {
                transitionsMap.put(WfUtils.encodeKey(transition.srcTerminal), transition.dstName);
            }
        }
        return transitionsMap;
    }

    private void checkDesignExist(String id) throws DesignCompilationException {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        em.setView(AppBeans.get(Metadata.class).getViewRepository().getView(Design.class, "for-subdesign-module"));
        design = em.find(Design.class, UUID.fromString(id));
        if (design == null) {
            throw new DesignCompilationException("Module : " + caption + ". Subdesign not found");
        }
    }
}
