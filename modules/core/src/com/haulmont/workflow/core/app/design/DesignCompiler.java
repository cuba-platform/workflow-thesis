/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.google.common.base.Preconditions;
import com.haulmont.bali.datastruct.Pair;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.*;
import com.haulmont.cuba.core.app.ServerInfo;
import com.haulmont.cuba.core.global.*;
import com.haulmont.workflow.core.app.CompilationMessage;
import com.haulmont.workflow.core.app.WfUtils;
import com.haulmont.workflow.core.app.design.modules.SubDesignModule;
import com.haulmont.workflow.core.entity.DesignProcessVariable;
import com.haulmont.workflow.core.error.DesignError;
import com.haulmont.workflow.core.error.DesignCompilationError;
import com.haulmont.workflow.core.error.ModuleError;
import com.haulmont.workflow.core.app.design.modules.StartModule;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.exception.TemplateGenerationException;
import com.haulmont.workflow.core.global.WfConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.dom4j.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.*;

import static com.haulmont.workflow.core.global.WfConstants.CARD_VARIABLES_SEPARATOR;

/**
 * @author krivopustov
 * @version $Id$
 */
public class DesignCompiler {

    protected Map<String, String> moduleClassNames;

    protected volatile Map<String, Class<? extends Module>> moduleClasses;

    protected FormCompiler formCompiler;

    protected DesignPostProcessor postProcessor;

    protected Messages messages;

    protected ServerInfo serverInfo;

    private Log log = LogFactory.getLog(DesignCompiler.class);

    // set from spring.xml
    public void setModuleClasses(Map<String, String> moduleClassNames) {
        this.moduleClassNames = moduleClassNames;
    }

    private Map<String, Class<? extends Module>> getModuleClasses() {
        if (moduleClasses == null) {
            synchronized (this) {
                moduleClasses = new HashMap<>();

                for (Map.Entry<String, String> entry : moduleClassNames.entrySet()) {
                    moduleClasses.put(entry.getKey(), AppBeans.get(Scripting.class).<Module>loadClass(entry.getValue()));
                }
            }
        }
        return moduleClasses;
    }

    // set from spring.xml
    public void setFormCompiler(FormCompiler formCompiler) {
        this.formCompiler = formCompiler;
    }

    //set from spring.xml
    public void setPostProcessor(DesignPostProcessor postProcessor) {
        this.postProcessor = postProcessor;
    }

    //set from spring.xml
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    //set from spring.xml
    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public CompilationMessage compileDesign(UUID designId) throws DesignCompilationException {
        List<DesignCompilationError> errors = new LinkedList<>();
        List<String> warnings = new LinkedList<>();
        Preconditions.checkArgument(designId != null, "designId is null");
        log.info("Compiling design " + designId);

        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        try {
            EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
            Design design = em.find(Design.class, designId);

            List<Module> modules = new ArrayList<>();

            errors.addAll(createModules(design, modules));

            errors.addAll(checkRequiredModules(modules));

            cleanup(design);

            Collection<DesignProcessVariable> designProcessVariables = getProcessVariables(modules);

            saveParameters(design, designProcessVariables);

            String jpdl = compileJpdl(modules, errors);

            if (BooleanUtils.isFalse(checkEndReachable(jpdl))) {
                errors.add(new DesignError(AppBeans.get(Messages.class).getMessage(getClass(), "exception.unreachableEndOfTheProcess")));
            }

            Map<String, String> unusedModules = checkUnusedModules(jpdl, modules);
            List<DesignCompilationError> errorsToRemove = new LinkedList<>();
            for (DesignCompilationError error : errors) {
                if (error instanceof ModuleError) {
                    String name = ((ModuleError) error).getModuleName();
                    for (String unusedName : unusedModules.keySet()) {
                        if (ObjectUtils.equals(name, unusedName)) {
                            errorsToRemove.add(error);
                            break;
                        }
                    }
                }
            }
            errors.removeAll(errorsToRemove);

            if (unusedModules.size() > 0) {
                StringBuilder modulesList = new StringBuilder();
                for (String moduleName : unusedModules.values()) {
                    String moduleNameCaption = moduleName == null ?
                            AppBeans.get(Messages.class).getMessage(getClass(), "unnamed") : moduleName;

                    modulesList.append("<li>").append(StringEscapeUtils.escapeHtml(moduleNameCaption)).append("</li>");
                }
                warnings.add(AppBeans.get(Messages.class).formatMessage(getClass(),
                        "warning.unusedModules", modulesList.toString()));
            }

            String forms = compileForms(modules, errors);

            if (errors.size() > 0) {
                return new CompilationMessage(errors, warnings);
            }

            saveDesignFile(design, "", "jpdl", jpdl, null);
            saveDesignFile(design, "", "forms", forms, null);

            Element localization = null;
            if (!StringUtils.isBlank(design.getLocalization())) {
                Document document = Dom4j.readDocument(design.getLocalization());
                localization = document.getRootElement();
            }

            String messagesEn = compileMessages(modules, "en", localization);
            saveDesignFile(design, "messages.properties", "messages", messagesEn, null);

            String messagesRu = compileMessages(modules, "ru", localization);
            saveDesignFile(design, "messages_ru.properties", "messages", messagesRu, null);

            if (BooleanUtils.isTrue(design.getNotificationMatrixUploaded())
                    && design.getNotificationMatrix().length > 0)
                saveDesignFile(design, "", "notification", null, design.getNotificationMatrix());

            design.setCompileTs(AppBeans.get(TimeSource.class).currentTimestamp());

            tx.commit();

            log.info("Design " + designId + " succesfully compiled");
            return new CompilationMessage(errors, warnings);
        } catch (JSONException e) {
            errors.add(new DesignError(e.getMessage()));
            return new CompilationMessage(errors, warnings);
        } finally {
            tx.end();
        }
    }

    private List<DesignCompilationError> checkRequiredModules(List<Module> modules) {
        boolean startExist = false;
        List<DesignCompilationError> errors = new LinkedList<>();
        for (Module module : modules) {
            if (StartModule.class.isAssignableFrom(module.getClass())) {
                startExist = true;
            }
        }
        if (!startExist)
            errors.add(new DesignError(AppBeans.get(Messages.class).getMessage(getClass(), "exception.StartModuleNotExist")));
        return errors;
    }

    private Boolean checkEndReachable(String jpdl) {
        Map<String, Element> modulesByName = getModulesByName(jpdl);
        List<String> visitedModules = new ArrayList<>();
        List<String> visitingModules = new ArrayList<>();
        Element startEl = getStartElement(modulesByName.values());
        if (startEl == null)
            return null;
        List<Element> transitions = startEl.elements("transition");
        for (Element transition : transitions) {
            String nextModuleName = transition.attributeValue("to");
            if (visitNext(nextModuleName, modulesByName, visitedModules, visitingModules))
                return true;
        }
        return false;
    }

    private boolean visitNext(String moduleName, Map<String, Element> modulesByName, List<String> visitedModules, List<String> visitingModules) {
        if (moduleName == null)
            return false;
        visitedModules.add(moduleName);
        Element el = modulesByName.get(moduleName);
        if ("end".equals(el.getName())) {
            return true;
        }
        List<Element> transitions = el.elements("transition");
        for (Element transition : transitions) {
            String nextModuleName = transition.attributeValue("to");
            if (!visitedModules.contains(nextModuleName) && !visitingModules.contains(nextModuleName)) {
                if (visitNext(nextModuleName, modulesByName, visitedModules, visitingModules))
                    return true;
            }
        }
        visitedModules.add(moduleName);
        return false;
    }

    private Map<String, Element> getModulesByName(String jpdl) {
        Map<String, Element> modulesByName = new HashMap<>();
        Document document;
        try {
            document = DocumentHelper.parseText(jpdl);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
        List<Element> modulesEl = document.getRootElement().elements();
        for (Element moduleEl : modulesEl) {
            String name = moduleEl.attributeValue("name");
            if (name != null)
                modulesByName.put(name, moduleEl);
        }
        return modulesByName;
    }

    private Element getStartElement(Collection<Element> elements) {
        for (Element element : elements) {
            if ("start".equals(element.getName())) {
                return element;
            }
        }
        return null;
    }

    private Map<String, String> checkUnusedModules(String jpdl, List<Module> modules) {
        Map<String, String> errors = new HashMap<>();

        Map<String, Element> modulesByName;
        Map<String, String> modulesNames = new HashMap<>();
        for (Module module : modules) {
            modulesNames.put(module.getName(), module.getCaption());
        }

        modulesByName = getModulesByName(jpdl);
        //add to map modules not from JSON
        for (String moduleName : modulesByName.keySet()) {
            if (!modulesNames.containsKey(moduleName)) {
                modulesNames.put(moduleName, moduleName);
            }
        }

        Element startElement = getStartElement(modulesByName.values());

        if (startElement != null) {
            modulesNames.remove(startElement.attributeValue("name"));
            modulesByName.remove(startElement.attributeValue("name"));
            findNext(startElement, modulesByName, modulesNames);
            errors.putAll(modulesNames);
        }
        return errors;
    }

    private void findNext(Element element, Map<String, Element> modulesByName, Map<String, String> modulesNames) {
        List<Element> transitionsEl = element.elements("transition");
        for (Element transitionEl : transitionsEl) {
            String moduleName = transitionEl.attributeValue("to");
            if (modulesNames.containsKey(moduleName)) {
                modulesNames.remove(moduleName);
                Element nextModuleEl = modulesByName.get(moduleName);
                if (nextModuleEl != null) {
                    findNext(nextModuleEl, modulesByName, modulesNames);
                }
                modulesByName.remove(moduleName);
            }
        }
    }

    private Collection<DesignProcessVariable> getProcessVariables(List<Module> modules) throws DesignCompilationException {
        Map<String, DesignProcessVariable> designProcessVariables = new HashMap<>();
        for (Module module : modules) {
            for (DesignProcessVariable processVariable : module.generateDesignProcessVariables()) {
                if (!designProcessVariables.containsKey(processVariable.getAlias())) {
                    designProcessVariables.put(processVariable.getAlias(), processVariable);
                } else {
                    DesignProcessVariable designProcessVariable = designProcessVariables.get(processVariable.getAlias());
                    if (isSameVariable(processVariable, designProcessVariable)) {
                        designProcessVariable.setModuleName(designProcessVariable.getModuleName()
                                + CARD_VARIABLES_SEPARATOR + processVariable.getModuleName());
                        designProcessVariable.setPropertyName(designProcessVariable.getPropertyName()
                                + CARD_VARIABLES_SEPARATOR + processVariable.getPropertyName());
                        if (designProcessVariable.getAttributeType() == null) {
                            designProcessVariable.setAttributeType(processVariable.getAttributeType());
                            designProcessVariable.setMetaClassName(processVariable.getMetaClassName());
                        }

                        if (StringUtils.isBlank(designProcessVariable.getValue())) {
                            designProcessVariable.setValue(processVariable.getValue());
                        }
                        Set<String> tags = processVariable.getTagsFromComment();
                        designProcessVariable.addTagsToComment(tags);
                    } else {
                        throw new DesignCompilationException(String.format(AppBeans.get(Messages.class).getMessage(getClass(),
                                "variablesWithoutSameAttributeType"), designProcessVariable.getAlias()));
                    }
                }
            }
        }
        return designProcessVariables.values();
    }

    private boolean isSameVariable(DesignProcessVariable processVariable, DesignProcessVariable designProcessVariable) {
        if (designProcessVariable.getAttributeType() == null || processVariable.getAttributeType() == null) return true;
        return designProcessVariable.getAttributeType() == processVariable.getAttributeType()
                && ObjectUtils.equals(designProcessVariable.getMetaClassName(), processVariable.getMetaClassName());
    }

    private List<String> parseRoles(Document document) {
        List<String> rolesList = new LinkedList<>();
        List<Element> elements = document.getRootElement().elements("custom");
        for (Element e : elements) {
            List<Element> properties = e.elements("property");
            for (Element prop : properties) {
                if (prop.attribute("name").getValue().startsWith("role")) {
                    String role = prop.element("string").attribute("value").getValue();
                    if (!rolesList.contains(role)) {
                        rolesList.add(role);
                    }
                }

            }
        }
        return rolesList;
    }

    private Map<String, String> parseStates(Document document, Properties properties) throws UnsupportedEncodingException, TemplateGenerationException {
        Map<String, String> states = new LinkedHashMap<>();
        List<Element> elements = document.getRootElement().elements();
        for (Element element : elements) {
            String elementKey = element.attributeValue("name");
            if (elementKey != null && (element.getName().equals("start")
                    || (element.getName().equals("join")) || checkState(elementKey, elements))) {
                List<Element> transitions = element.elements("transition");
                for (Element transition : transitions) {
                    String stateKey = transition.attributeValue("to");
                    String nextStates = getNextAssignmentStates(stateKey, elements, new ArrayList<String>());
                    if (nextStates != null) {
                        String[] statesKeys = StringUtils.split(nextStates, ",");
                        for (String nextStateKey : statesKeys) {
                            String stateName = properties.getProperty(elementKey) + '.' + properties.getProperty(nextStateKey);
                            states.put(nextStateKey + ", " + elementKey + '.' + nextStateKey, stateName);
                        }
                    }
                }
            }
        }
        states.put("Canceled", properties.getProperty("Canceled"));
        states.put("Reassign", properties.getProperty("Reassign"));
        postProcessor.processStates(states, document, properties);
        return states;
    }

    private String getNextAssignmentStates(String stateKey, List<Element> elements, List<String> previosElements) throws TemplateGenerationException {
        for (Element element : elements) {
            Attribute nameAttr = element.attribute("name");
            if (nameAttr != null && stateKey.equals(nameAttr.getValue())) {

                if (previosElements.contains(stateKey))
                    return null;
                if (checkJoin(stateKey, elements))
                    return null;
                if (checkState(stateKey, elements)) {
                    return stateKey;
                } else {
                    previosElements.add(stateKey);
                    List<Element> transitions = element.elements("transition");
                    StringBuilder correctStates = new StringBuilder();
                    for (Element transition : transitions) {
                        String nextState = transition.attributeValue("to");
                        String nextCorrectState = getNextAssignmentStates(nextState, elements, previosElements);
                        if (nextCorrectState != null) {
                            correctStates.append(nextCorrectState + ",");
                        }
                    }
                    return correctStates.toString();
                }
            }
        }
        return null;
    }

    private boolean checkState(String stateKey, List<Element> elements) {
        for (Element element : elements) {
            Attribute nameAttr = element.attribute("name");
            if (nameAttr != null && stateKey.equals(nameAttr.getValue())) {
                Attribute classAttr = element.attribute("class");
                if (element.getName().equals("end")) {
                    return true;
                }
                if (classAttr != null) {
                    Class assignerClass = AppBeans.get(Scripting.class).loadClass("com.haulmont.workflow.core.activity.Assigner");
                    Class currentClass = AppBeans.get(Scripting.class).loadClass(classAttr.getValue());
                    if (assignerClass.isAssignableFrom(currentClass))
                        return true;
                }

            }
        }
        return false;
    }

    private boolean checkJoin(String elementName, List<Element> elements) throws TemplateGenerationException {
        for (Element element : elements) {
            if (elementName.equals(element.attributeValue("name"))) {
                if ("join".equals(element.getName())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new TemplateGenerationException("Element with name " + elementName + " not found in jpdl xml");
    }


    private void createStatesSheet(Workbook book, Map<String, String> statesMap) {
        Sheet statesSheet = book.getSheet("States");

        Set<Map.Entry<String, String>> set = statesMap.entrySet();
        Iterator<Row> rowIt = statesSheet.rowIterator();
        Iterator<Map.Entry<String, String>> stateIt = set.iterator();
        Map.Entry<String, String> stateEntry;
        while (rowIt.hasNext()) {
            Row row = rowIt.next();
            if (stateIt.hasNext()) {
                stateEntry = stateIt.next();
                row.getCell(0).setCellValue(stateEntry.getValue());
                row.getCell(1).setCellValue(stateEntry.getKey());
            } else {
                row.removeCell(row.getCell(0));
                row.removeCell(row.getCell(1));
            }
        }

    }

    private void createRolesSheet(Workbook book, List<String> rolesList) {

        Sheet roles = book.getSheet("Roles");
        int i = 0;
        Iterator<String> roleIt = rolesList.iterator();
        Iterator<Row> rowIt = roles.rowIterator();
        while (rowIt.hasNext()) {
            Row row = rowIt.next();
            if (roleIt.hasNext()) {
                String role = roleIt.next();
                row.getCell(0).setCellValue(role);
                row.getCell(1).setCellValue(role);
            } else {
                row.removeCell(row.getCell(0));
                row.removeCell(row.getCell(1));
            }
        }

    }

    private void createNotificationSheet(Workbook book, List<String> rolesList, Collection<String> states, String sheetName) {
        Sheet mail = book.getSheet(sheetName);
        if (mail != null) {
            Row statesRow = mail.getRow(1);
            Iterator<Cell> cellIt = statesRow.cellIterator();
            cellIt.next();
            Iterator<String> statesIt = states.iterator();
            while (cellIt.hasNext()) {
                Cell cell = cellIt.next();
                if (statesIt.hasNext()) {
                    cell.setCellValue(statesIt.next());
                } else {
                    statesRow.removeCell(cell);
                }
            }
            Iterator<Row> rowIt = mail.rowIterator();
            rowIt.next();
            rowIt.next();
            Iterator<String> roleIt = rolesList.iterator();
            while (rowIt.hasNext()) {
                Row row = rowIt.next();
                if (roleIt.hasNext()) {
                    row.getCell(0).setCellValue(roleIt.next());
                    Iterator<Cell> cellIterator = row.cellIterator();
                    cellIterator.next();
                    while (cellIterator.hasNext()) {
                        row.removeCell(cellIterator.next());
                    }

                } else {
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        row.removeCell(cellIterator.next());
                    }
                }
            }
        }

    }


    public byte[] compileXlsTemplate(UUID designId) throws TemplateGenerationException {
        Transaction tx = AppBeans.get(Persistence.class).createTransaction();
        List<DesignFile> files = null;
        try {
            EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
            Query q = em.createQuery();
            q.setQueryString("select  df from wf$DesignFile df where df.design.id=?1 and (df.type='messages' or df.type='jpdl' )");
            q.setParameter(1, designId);
            files = q.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
        try {

            Locale locale = AppBeans.get(UserSessionSource.class).getLocale();
            String lang = locale.getLanguage();
            String fileName = "messages" + ("en".equals(lang) ? ("") : ("_" + lang)) + ".properties";
            //Find current locale messages
            String messages = null;
            for (DesignFile file : files) {
                if (file.getName().equals(fileName)) {
                    messages = file.getContent();
                    break;
                }
            }

            Properties properties = new Properties();
            properties.load(new StringReader(messages));

            String jpdl = null;
            for (DesignFile file : files) {
                if ("jpdl".equals(file.getType())) {
                    jpdl = file.getContent();
                    break;
                }
            }

            Document document = DocumentHelper.parseText(jpdl);
            List<String> rolesList = parseRoles(document);
            Map<String, String> states = parseStates(document, properties);

            String templatePath = AppBeans.get(Configuration.class).getConfig(WfConfig.class).getNotificationTemplatePath();
            InputStream stream = AppBeans.get(Resources.class).getResourceAsStream(templatePath);
            Workbook wb;
            try {
                wb = new HSSFWorkbook(stream);
                createRolesSheet(wb, rolesList);
                createStatesSheet(wb, states);
                createNotificationSheet(wb, rolesList, states.values(), "Mail");
                createNotificationSheet(wb, rolesList, states.values(), "Tray");
                createNotificationSheet(wb, rolesList, states.values(), "Sms");
            } finally {
                IOUtils.closeQuietly(stream);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            wb.write(buffer);
            return buffer.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new TemplateGenerationException(e);
        } finally {
            tx.end();
        }
    }


    private List<DesignCompilationError> createModules(Design design, List<Module> modules) throws JSONException, DesignCompilationException {
        List<DesignCompilationError> errors = new LinkedList<>();
        List<String> modulesNames = new ArrayList<>();

        JSONObject json = new JSONObject(design.getSrc());

        JSONObject jsWorking = json.getJSONObject("working");
        JSONArray jsModules = null;
        try {
            jsModules = jsWorking.getJSONArray("modules");
        } catch (JSONException e) {
            return errors;
        }
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
            try {
                module.init(new Module.Context(design, jsModule, formCompiler));
            } catch (DesignCompilationException e) {
                errors.add(new ModuleError(module.getName(), e.getMessage()));
            }
            if (module.getName() != null && modulesNames.contains(module.getName())) {
                errors.add(new DesignError(messages.formatMessage(getClass(),
                        "exception.duplicateModuleName", StringEscapeUtils.escapeHtml(module.getCaption()))));
            } else
                modulesNames.add(module.getName());
            modules.add(module);
        }

        JSONArray jsWires = jsWorking.getJSONArray("wires");
        try {
            addTransitions(modules, jsModules, jsWires, errors);
        } catch (DesignCompilationException e) {
            errors.add(new DesignError(e.getMessage()));
        }
        return errors;
    }

    private void addTransitions(List<Module> modules, JSONArray jsModules, JSONArray jsWires, List<DesignCompilationError> errors) throws JSONException, DesignCompilationException {
        Map<Integer, Module> otherModules = new HashMap<>();

        for (int i = 0; i < modules.size(); i++) {
            //Key - terminal name
            Map<String, List<TransitionParams>> moduleTransitionsParams = getModuleTransitionsParams(modules.get(i), i, jsWires);
            JSONObject jsModule = jsModules.getJSONObject(i);
            JSONArray outputs;
            try {
                outputs = jsModule.getJSONObject("value").getJSONArray("outputs");
            } catch (JSONException e) {
                //If module haven't outputs array
                otherModules.put(i, modules.get(i));
                continue;
            }

            for (int j = 0; j < outputs.length(); j++) {
                String terminalName = outputs.getJSONObject(j).getString("name");
                List<TransitionParams> currParamsList = moduleTransitionsParams.get(terminalName);
                if (currParamsList == null) {
                    if (ObjectUtils.equals("Fork", jsModule.get("name"))) {
                        continue;
                    }
                    errors.add(new ModuleError(WfUtils.encodeKey(jsModule.getJSONObject("value").getString("name")),
                            AppBeans.get(Messages.class).formatMessage(
                                    getClass(),
                                    "exception.emptyTransition",
                                    StringEscapeUtils.escapeHtml(jsModule.getJSONObject("value").getString("name")))));
                    continue;
                }
                for (TransitionParams currParams : currParamsList) {
                    modules.get(i).addTransition(terminalName,
                            modules.get(currParams.dstModuleId),
                            currParams.dstModuleTerminal);
                }
                moduleTransitionsParams.remove(currParamsList);
            }
        }
        //Other modules(without outputs array)
        //Now all modules have outputs array,but shouldn't delete for compatibility
        Set<Map.Entry<Integer, Module>> otherModulesSet = otherModules.entrySet();
        for (Map.Entry<Integer, Module> entry : otherModulesSet) {
            Map<String, List<TransitionParams>> moduleTransitionsParams = getModuleTransitionsParams(entry.getValue(), entry.getKey(), jsWires);

            Set<Map.Entry<String, List<TransitionParams>>> set = moduleTransitionsParams.entrySet();
            for (Map.Entry<String, List<TransitionParams>> transitionParamsEntry : set) {
                for (TransitionParams currentParams : transitionParamsEntry.getValue()) {
                    entry.getValue().addTransition(transitionParamsEntry.getKey(),
                            modules.get(currentParams.dstModuleId),
                            currentParams.dstModuleTerminal);
                }
            }
        }
    }

    private Map<String, List<TransitionParams>> getModuleTransitionsParams(Module module, int moduleId, JSONArray jsWires) throws JSONException {
        Map<String, List<TransitionParams>> wires = new HashMap<>();

        for (int i = 0; i < jsWires.length(); i++) {
            JSONObject jsWire = jsWires.getJSONObject(i);
            JSONObject jsWireSrc = jsWire.getJSONObject("src");
            if (moduleId == jsWireSrc.getInt("moduleId")) {
                JSONObject jsWireDst = jsWire.getJSONObject("tgt");
                if (wires.containsKey(jsWireSrc.getString("terminal"))) {
                    wires.get(jsWireSrc.getString("terminal")).add(
                            new TransitionParams(jsWireDst.getInt("moduleId"), jsWireDst.getString("terminal")));
                } else {
                    TransitionParams params = new TransitionParams(jsWireDst.getInt("moduleId"), jsWireDst.getString("terminal"));
                    List<TransitionParams> list = new LinkedList<>();
                    list.add(params);
                    wires.put(jsWireSrc.getString("terminal"), list);
                }
            }
        }

        return wires;
    }

    private class TransitionParams {
        TransitionParams(int dstModuleId, String dstModuleTerminal) {
            this.dstModuleId = dstModuleId;
            this.dstModuleTerminal = dstModuleTerminal;
        }

        int dstModuleId;
        String dstModuleTerminal;
    }

    private void cleanup(Design design) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();

        Query q = em.createQuery("delete from wf$DesignFile df where df.design.id = ?1");
        q.setParameter(1, design.getId());
        q.executeUpdate();
    }

    private void saveParameters(Design design, Collection<DesignProcessVariable> designProcessVariables) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();

        Map<Pair<String, String>, DesignProcessVariable> designProcessVariableMap = new HashMap<>();
        for (DesignProcessVariable processVariable : designProcessVariables) {
            designProcessVariableMap.put(new Pair<>(processVariable.getModuleName(), processVariable.getAlias()), processVariable);
        }

        Query existsParametersQuery = em.createQuery("select dp from wf$DesignProcessVariable dp where dp.design.id = :designId");
        existsParametersQuery.setParameter("designId", design.getId());
        existsParametersQuery.setView(AppBeans.get(Metadata.class).getViewRepository().getView(DesignProcessVariable.class, View.LOCAL));
        List<DesignProcessVariable> existsDesignProcessVariables = existsParametersQuery.getResultList();

        for (DesignProcessVariable exists : existsDesignProcessVariables) {
            exists.setComment(exists.getCommentWithoutTags());
        }

        for (DesignProcessVariable exists : existsDesignProcessVariables) {
            DesignProcessVariable designProcessVariable = designProcessVariableMap.get(new Pair<>(exists.getModuleName(), exists.getAlias()));
            if (designProcessVariable != null) {
                if (BooleanUtils.isNotTrue(exists.getOverridden())) {
                    exists.setValue(designProcessVariable.getValue());
                    exists.setModuleName(designProcessVariable.getModuleName());
                    exists.setPropertyName(designProcessVariable.getPropertyName());
                }
                Set<String> tags = designProcessVariable.getTagsFromComment();
                exists.addTagsToComment(tags);
                designProcessVariables.remove(designProcessVariable);
                em.merge(exists);
            } else if (StringUtils.isNotBlank(exists.getModuleName())) {
                em.remove(exists);
            }
        }

        for (DesignProcessVariable designProcessVariable : designProcessVariables) {
            designProcessVariable.setDesign(design);
            em.persist(designProcessVariable);
        }
    }

    private void saveDesignFile(Design design, String name, String type, String content, byte[] binaryContent) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();

        DesignFile df = new DesignFile();
        df.setDesign(design);
        df.setContent(content);
        df.setBinaryContent(binaryContent);
        df.setName(name);
        df.setType(type);
        em.persist(df);
    }

    private String compileJpdl(List<Module> modules, List<DesignCompilationError> compileErrors) throws DesignCompilationException {
        Document document = DocumentHelper.createDocument();
        Element rootEl = document.addElement("process", "http://jbpm.org/4.2/jpdl");

        for (Module module : modules) {
            try {
                module.writeJpdlXml(rootEl);
            } catch (DesignCompilationException e) {
                compileErrors.add(new ModuleError(module.getName(), e.getMessage()));
            }
        }

        Element endEl = rootEl.addElement("on");
        endEl.addAttribute("event", "end");
        Element endListenerEl = endEl.addElement("event-listener");
        endListenerEl.addAttribute("class", "com.haulmont.workflow.core.activity.EndProcessListener");

        Element startEl = rootEl.addElement("on");
        startEl.addAttribute("event", "start");
        Element startListenerEl = startEl.addElement("event-listener");
        startListenerEl.addAttribute("class", "com.haulmont.workflow.core.activity.StartProcessListener");

        processSubdesignJpdl(rootEl);
        postProcessor.processJpdl(rootEl, compileErrors);
        return Dom4j.writeDocument(document, true);
    }

    public void processSubdesignJpdl(Element rootElement) {
        Map<String, String> subDesigns = new HashMap<>();
        List<Element> subDesign = (List<Element>) rootElement.elements(SubDesignModule.SUBDESIGN_ELEMENT_NAME);
        if (!subDesign.isEmpty()) {
            for (Element element : subDesign) {
                subDesigns.put(element.attributeValue("name"), element.attributeValue("startTransitionName"));
            }
            for (Element element : (List<Element>) rootElement.elements()) {
                List<Element> transitions = element.elements("transition");
                if (!transitions.isEmpty()) {
                    for (Element transition : transitions) {
                        String to = transition.attributeValue("to");
                        if (StringUtils.isNotBlank(to)) {
                            String newDest = subDesigns.get(to);
                            if (newDest != null) {
                                transition.addAttribute("to", newDest);
                            }
                        }
                    }
                }
            }
        }
    }

    private String compileMessages(List<Module> modules, String lang, Element localization) {
        Properties properties = new Properties();

        Locale locale = new Locale(lang);
        String mp = "com.haulmont.workflow.core.app.design";

        compileMessage(properties, locale, "SAVE_ACTION", mp);
        compileMessage(properties, locale, "SAVE_AND_CLOSE_ACTION", mp);
        compileMessage(properties, locale, "START_PROCESS_ACTION", mp);
        compileMessage(properties, locale, "CANCEL_PROCESS_ACTION", mp);
        compileMessage(properties, locale, "REASSIGN_ACTION", mp);
        compileMessage(properties, locale, "Canceled", mp);
        compileMessage(properties, locale, "Reassign", mp);

        for (Module module : modules) {
            module.writeMessages(properties, lang);
        }

        postProcessor.processMessages(properties, locale);

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
        properties.setProperty(key, AppBeans.get(Messages.class).getMessage(messagePack, key, locale));
    }

    private String compileForms(List<Module> modules, List<DesignCompilationError> errors) {
        Document document = DocumentHelper.createDocument();
        Element rootEl = document.addElement("forms", "http://schemas.haulmont.com/workflow/" + getPlatformVersion() + "/forms.xsd");

        for (Module module : modules) {
            try {
                module.writeFormsXml(rootEl);
            } catch (DesignCompilationException e) {
                errors.add(new DesignError(e.getMessage()));
            }
        }
        postProcessor.processForms(rootEl, modules, errors);
        return Dom4j.writeDocument(document, true);
    }

    private String getPlatformVersion() {
        String releaseNumber = serverInfo.getReleaseNumber();
        int dashIndex = releaseNumber.indexOf("-");
        if (dashIndex != -1) {
            return releaseNumber.substring(0, dashIndex);
        } else {
            int secondDotIndex = StringUtils.ordinalIndexOf(releaseNumber, ".", 2);
            if (secondDotIndex != -1) {
                return releaseNumber.substring(0, secondDotIndex);
            }
        }
        return "5.3";
    }

    public Map<String, Properties> compileMessagesForLocalization(Design design, List<String> languages) throws DesignCompilationException {
        Map<String, Properties> result = new HashMap<>();

        List<Module> modules = new ArrayList<>();
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
