/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.gui.app.design;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ParamsMap;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.global.UserSessionSource;
import com.haulmont.cuba.gui.components.AbstractEditor;
import com.haulmont.cuba.gui.data.CollectionDatasource;
import com.haulmont.cuba.gui.data.Datasource;
import com.haulmont.workflow.core.app.DesignerService;
import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignLocKey;
import com.haulmont.workflow.core.entity.DesignLocValue;
import com.haulmont.workflow.core.exception.DesignCompilationException;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.inject.Inject;
import java.util.*;

public class LocalizeDesignWindow extends AbstractEditor {

    private Properties properties;
    private Map<String, Properties> langToMessages;

    @Inject
    private Datasource<Design> designDs;
    @Inject
    private CollectionDatasource<DesignLocKey, UUID> keysDs;
    @Inject
    private CollectionDatasource<DesignLocValue, UUID> valuesDs;

    @Inject
    protected DesignerService service;

    @Inject
    protected Configuration configuration;

    @Inject
    protected UserSessionSource userSessionSource;

    private Document document;
    private Element rootEl;
    private BiMap<Element, DesignLocKey> keysMap;
    private BiMap<Element, DesignLocValue> valuesMap;

    private enum Type {
        ACTION("action"),
        STATE("state"),
        TRANSITION("transition"),
        RESULT("result"),
        DESCRIPTION("description");

        private String aType;

        Type(String result) {
            this.aType = result;
        }

        public String type(){
            return this.aType;
        }
    }

    public LocalizeDesignWindow() {
        keysMap = HashBiMap.create();
        valuesMap = HashBiMap.create();
    }

    @Override
    public void setItem(Entity item) {
        super.setItem(item);

        Map<String,Locale> locales = configuration.getConfig(GlobalConfig.class).getAvailableLocales();
        List<String> languages = new ArrayList<>(locales.size());
        for (Locale locale : locales.values()) {
            languages.add(locale.toString());
        }

        Design design = (Design) getItem();

        try {
            langToMessages = service.compileMessagesForLocalization(design, languages);
        } catch (DesignCompilationException e) {
            showNotification(
                    getMessage("notification.compileFailed"),
                    e.getMessage(),
                    NotificationType.ERROR
            );
            return;
        }
        if(langToMessages.isEmpty())
            return;

        String lang = userSessionSource.getLocale().toString();
        properties = langToMessages.get(lang);
        if (properties == null)
            properties = langToMessages.values().iterator().next();

        if (!StringUtils.isBlank(design.getLocalization())) {
            document = Dom4j.readDocument(design.getLocalization());
            rootEl = document.getRootElement();
        } else {
            document = DocumentHelper.createDocument();
            rootEl = document.addElement("localization");
        }

        fillKeys();
        ArrayList<DesignLocKey> keys = new ArrayList<>(keysMap.values());
        Collections.sort(keys);

        Map<String, Object> keysDsParams = new HashMap<>();
        keysDsParams.put("keysCollection", keys);

        keysDs.addItemChangeListener(e -> {
            valuesDs.refresh(ParamsMap.of("valuesCollection", getValues(e.getItem())));
        });

        valuesDs.addItemPropertyChangeListener(e -> {
            Element el = valuesMap.inverse().get(e.getItem());
            if (el != null) {
                el.setText((String) e.getValue());

                String xml = Dom4j.writeDocument(document, true);
                Design design1 = designDs.getItem();
                design1.setLocalization(xml);
                design1.setCompileTs(null);
            }
        });

        keysDs.refresh(keysDsParams);
    }

    private void fillKeys() {
        iterateXml(rootEl, null);
        iterateProperties(rootEl);
    }

    private Collection<DesignLocValue> getValues(DesignLocKey designLocKey) {
        if (designLocKey == null)
            return null;
        ArrayList<DesignLocValue> result = new ArrayList<>();

        Element keyEl = keysMap.inverse().get(designLocKey);
        for (String lang : langToMessages.keySet()) {
            Element valueEl = null;
            for (Element el : Dom4j.elements(keyEl, "value")) {
                if (lang.equals(el.attributeValue("lang"))) {
                    valueEl = el;
                    break;
                }
            }
            if (valueEl == null) {
                valueEl = keyEl.addElement("value");
                valueEl.addAttribute("lang", lang);
                valueEl.setText(langToMessages.get(lang).getProperty(designLocKey.getName(), ""));
            }
            DesignLocValue designLocValue = valuesMap.get(valueEl);
            if (designLocValue == null) {
                designLocValue = new DesignLocValue();
                designLocValue.setLang(lang);
                designLocValue.setMessage(valueEl.getText());
                valuesMap.put(valueEl, designLocValue);
            }

            result.add(designLocValue);
        }

        return result;
    }

    private void iterateXml(Element element, DesignLocKey parent) {
        for (Element keyEl : Dom4j.elements(element, "key")) {
            String id = keyEl.attributeValue("id");
            String propName = getKeyPath(parent, id);
            String description = "";

            if (WfConstants.ACTION_START.equals(id) ||
                    WfConstants.ACTION_SAVE_AND_CLOSE.equals(id) ||
                    WfConstants.ACTION_SAVE.equals(id) ||
                    WfConstants.ACTION_CANCEL.equals(id)) {
                description = Type.ACTION.type();
            } else if (id.equals("description")) {
                description = Type.DESCRIPTION.type();
            } else if (id.equals("Result")) {
                description = Type.RESULT.type();
            } else if (parent == null) {
                description = Type.STATE.type();
            } else if (parent.getParentKey() == null) {
                description = Type.TRANSITION.type();
            }
            String property = properties.getProperty(propName);
            DesignLocKey designLocKey = null;
            if (property != null) {
                designLocKey = new DesignLocKey();
                designLocKey.setKey(id);
                designLocKey.setParentKey(parent);
                designLocKey.setCaption(property+' '+ '('+getMessage(description)+')');

                keysMap.put(keyEl, designLocKey);
            }

            iterateXml(keyEl, designLocKey);
        }
    }

    private String getKeyPath(DesignLocKey parent, String id) {
        if (parent == null)
            return id;
        else {
            return getKeyPath(parent.getParentKey(), parent.getKey()) + "." + id;
        }
    }

    private void iterateProperties(Element rootEl) {
        for (String propName : properties.stringPropertyNames()) {
            String[] parts = propName.split("\\.");
            Element element = rootEl;
            StringBuilder path = new StringBuilder();
            int level=0;
            for (String part : parts) {
                Element partEl = null;
                if (path.length() > 0)
                    path.append(".");
                path.append(part);

                for (Element el : Dom4j.elements(element, "key")) {
                    if (el.attributeValue("id").equals(part)) {
                        partEl = el;
                        break;
                    }
                }

                String description = "";

                if (WfConstants.ACTION_START.equals(part) ||
                        WfConstants.ACTION_SAVE_AND_CLOSE.equals(part) ||
                        WfConstants.ACTION_SAVE.equals(part) ||
                        WfConstants.ACTION_CANCEL.equals(part)) {
                    description = Type.ACTION.type();
                } else if (part.equals("description")) {
                    description = Type.DESCRIPTION.type();
                } else if (part.equals("Result")) {
                    description = Type.RESULT.type();
                } else if (level == 0) {
                    description = Type.STATE.type();
                } else if (level == 1) {
                    description = Type.TRANSITION.type();
                }

                if (partEl == null) {
                    partEl = element.addElement("key");
                    partEl.addAttribute("id", part);

                    DesignLocKey designLocKey = new DesignLocKey();
                    designLocKey.setKey(part);
                    designLocKey.setParentKey(keysMap.get(element));
                    designLocKey.setCaption(properties.getProperty(path.toString(), part)+' '+ '('+getMessage(description)+')');

                    keysMap.put(partEl, designLocKey);
                }
                element = partEl;
                level++;
            }
        }
    }
}