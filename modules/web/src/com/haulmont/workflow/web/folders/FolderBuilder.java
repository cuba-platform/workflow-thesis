/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.folders;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.global.DevelopmentException;
import com.haulmont.cuba.core.global.QueryParserRegex;
import com.haulmont.cuba.core.global.Scripting;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.cuba.gui.data.DsContext;
import com.haulmont.cuba.gui.xml.XmlInheritanceProcessor;
import com.haulmont.cuba.gui.xml.data.DsContextLoader;
import com.haulmont.cuba.gui.xml.layout.LayoutLoader;
import com.haulmont.workflow.core.entity.ProcAppFolder;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>$Id$</p>
 *
 * @author pavlov
 */

public class FolderBuilder {
    private ProcAppFolder procAppFolder;
    public String entity;

    public FolderBuilder(ProcAppFolder procAppFolder) {
        this.procAppFolder = procAppFolder;
    }

    public void build() {
        Document doc = Dom4j.readDocument(procAppFolder.getProcAppFolderXml());
        Element rootElem = doc.getRootElement();

        entity = rootElem.elementText("entity");

        visibilityScript(rootElem);
        String procCondition = getProcCondition(rootElem);
        filter(entity, procCondition);
        quantityScript(entity, procCondition);
    }

    private void visibilityScript(Element element) {
        List<Element> roles = Dom4j.elements(element.element("roles"), "role");
        if (roles != null && roles.size() > 0) {
            StringBuilder sb = new StringBuilder("import com.haulmont.cuba.core.global.UserSessionProvider\n");
            sb.append("List<String> roles = (List<String>) UserSessionProvider.getUserSession().getRoles()\nreturn ");
            for (Element roleEl : roles) {
                sb.append("roles.contains('").append(roleEl.getText()).append("') ||\n");
            }
            sb.setLength(sb.length() - 3);
            procAppFolder.setVisibilityScript(sb.toString());
        }
    }

    private String getProcCondition(Element element) {
        StringBuilder sb = new StringBuilder("");
        for (Element conditionEl : Dom4j.elements(element.element("conditions"), "condition")) {
            String proc = conditionEl.elementText("proc");
            String states = conditionEl.elementText("states");
            boolean inExpr = Boolean.valueOf(conditionEl.elementText("inExpr"));
            if (proc != null || states != null) {
                sb.append("(");
                if (proc != null) {
                    sb.append("{E}.proc.code = '").append(proc).append("'");
                }
                if (proc != null && states != null) {
                    sb.append(" and ");
                }
                if (states != null) {
                    sb.append("(");
                    String[] ss = states.split(",");
                    for (String state : ss) {
                        if (inExpr) {
                            sb.append("{E}.state like '%,").append(state).append(",%' or ");
                        } else {
                            sb.append("{E}.state not like '%,").append(state).append(",%' and ");
                        }
                    }
                    sb.setLength(sb.length() - 4);
                    sb.append(")");
                }
                sb.append(") or ");
            }
        }

        if (sb.toString().endsWith("or ")) {
            sb.setLength(sb.length() - 4);
            sb.insert(0, "(").insert(sb.length() - 1, ")");
        }

        sb.append("and exists (select a from wf$Assignment a where a.card = {E} and ")
                .append("a.user.id = :session$userId and a.finished is null)");
        return sb.toString().replaceAll("\\{E\\}", getEntityAlias());
    }

    private void filter(String entityName, String procQuery) {
        String filterXml = procAppFolder.getFilterXml();
        if (filterXml != null) {
            Document doc = Dom4j.readDocument(filterXml);
            Element rootEl = doc.getRootElement();
            Element andEl = rootEl.element("and");
            if (andEl != null) {
                for (Element cEl : Dom4j.elements(andEl, "c")) {
                    if ("CUSTOM".equals(cEl.attributeValue("type")) &&
                            "PROC_APP_FOLDER".equals(cEl.attributeValue("locCaption"))) {
                        andEl.remove(cEl);
                        break;
                    }
                }
                andEl.add(createFilterCondition(procQuery));
                StringWriter writer = new StringWriter();
                Dom4j.writeDocument(doc, true, writer);
                filterXml = writer.toString();
            }
        } else {
            filterXml = createFilter(procQuery);
        }

        procAppFolder.setFilterXml(filterXml);
        procAppFolder.setFilterComponentId("[" + entityName + ".browse].genericFilter");
    }

    private String createFilter(String procCondition) {
        Document doc = DocumentHelper.createDocument();
        Element filterEl = doc.addElement("filter");
        Element andEl = filterEl.addElement("and");
        andEl.add(createFilterCondition(procCondition));

        StringWriter writer = new StringWriter();
        Dom4j.writeDocument(doc, true, writer);
        return writer.toString();
    }

    private Element createFilterCondition(String procCondition) {
        Element cEl = DocumentHelper.createElement("c").
                addAttribute("name", "xVZxBACBsT").
                addAttribute("unary", "true").
                addAttribute("hidden", "true").
                addAttribute("type", "CUSTOM").
                addAttribute("locCaption", "PROC_APP_FOLDER").
                addText(procCondition.replaceAll("\\\\", ""));
        cEl.addElement("param").
                addAttribute("name", "component$genericFilter.xVZxBACBsT13577").
                addText("true");
        return cEl;
    }

    private void quantityScript(String entityName, String procCondition) {
        StringBuilder sb = new StringBuilder("import com.haulmont.cuba.core.Locator\n");
        sb.append("import com.haulmont.cuba.core.EntityManager\n");
        sb.append("import com.haulmont.cuba.core.PersistenceProvider\n");
        sb.append("import com.haulmont.cuba.core.global.UserSessionProvider\n");
        sb.append("import com.haulmont.cuba.core.Query\n");
        sb.append("EntityManager em = PersistenceProvider.getEntityManager()\n");

        sb.append("Query q = em.createQuery(\"\"\"select count({E}.id) from ");
        sb.append(entityName);
        sb.append(" {E}\n");
        sb.append(procCondition != null ? " where " + procCondition : "");
        sb.append("\"\"\")\n");
        if (procCondition != null && procCondition.contains("session$userId")) {
            sb.append("q.setParameter('session$userId', UserSessionProvider.currentOrSubstitutedUserId())\n");
        }
        sb.append("def result = q.getSingleResult();\n");

        sb.append("q = em.createQuery(\"\"\"select count({E}.id) from wf$CardInfo ci\n");
        sb.append("join ci.card {E} where ci.deleteTs is null and ci.user.id = :session$userId ");
        sb.append(procCondition != null ? " and " + procCondition : "");
        sb.append("\"\"\")\n");
        sb.append("q.setParameter('session$userId', UserSessionProvider.currentOrSubstitutedUserId())\n");
        sb.append("def count = q.getSingleResult()\n");
        sb.append("style = (count != null && count > 0) ? 'cardremind' : null\n");
        sb.append("return result");

        procAppFolder.setQuantityScript(sb.toString().replace("{E}", getEntityAlias()).replace("$", "\\$"));
    }

    public String getEntityAlias(){
        Element element = getWindowXml();
        return parseWindowXml(element);
    }

    private String parseWindowXml(Element element) {
        String alias = null;
        DsContextLoader dsContextLoader = new DsContextLoader(null);
        DsContext dsContext = dsContextLoader.loadDatasources(element.element("dsContext"), null);
        for (Datasource ds : dsContext.getAll()) {
            if (entity.equals(ds.getMetaClass().getName()) && ds instanceof CollectionDatasource) {
                String query = ((CollectionDatasource) ds).getQuery();
                Pattern entityPattern = Pattern.compile(QueryParserRegex.ENTITY_PATTERN_REGEX, Pattern.CASE_INSENSITIVE);
                Matcher entityMatcher = entityPattern.matcher(query);
                while (entityMatcher.find()) {
                    if (entity.equals(entityMatcher.group(1))) {
                        alias = entityMatcher.group(3);
                        break;
                    }
                }
            }
        }
        return alias;
    }

    private Element getWindowXml() {
        WindowConfig windowConfig = AppContext.getBean(WindowConfig.class);
        WindowInfo windowInfo = windowConfig.getWindowInfo(entity + ".browse");
        Scripting scripting = AppContext.getBean(Scripting.NAME);
        String templatePath = windowInfo.getTemplate();

        InputStream stream = scripting.getResourceAsStream(templatePath);
        if (stream == null) {
            stream = getClass().getResourceAsStream(templatePath);
            if (stream == null)
                throw new DevelopmentException("Bad template path",
                        Collections.<String,Object>singletonMap("Template Path", templatePath));
        }

        Document document = null;
        try {
            document = LayoutLoader.parseDescriptor(stream, Collections.<String, Object>emptyMap());
        } finally {
            IOUtils.closeQuietly(stream);
        }
        XmlInheritanceProcessor processor = new XmlInheritanceProcessor(document, Collections.<String, Object>emptyMap());
        return processor.getResultRoot();
    }
}
