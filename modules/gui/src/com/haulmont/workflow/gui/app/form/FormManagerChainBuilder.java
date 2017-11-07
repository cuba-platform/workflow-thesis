/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.form;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.app.ResourceService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import com.haulmont.workflow.gui.base.action.FormManager;
import com.haulmont.workflow.gui.base.action.FormManagerChain;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import javax.annotation.ManagedBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chekashkin
 * @version $Id$
 */
@ManagedBean(FormManagerChainBuilder.NAME)
public class FormManagerChainBuilder {

    public static final String NAME = "workflow_formManagerChainBuilder";

    protected FormManagerChain nullObject = new FormManagerChain();
    protected Map<String, FormManagerChain> cache = new ConcurrentHashMap<>();

    protected Log log = LogFactory.getLog(FormManagerChainBuilder.class);

    public FormManagerChain build(Card card, String actionName) {
        if (card.getProc() == null)
            return nullObject;

        String resourceName = card.getProc().getProcessPath() + "/forms.xml";

        String cacheKey = resourceName + "/" + actionName;
        FormManagerChain cached = cache.get(cacheKey);
        if (cached != null) {
            cached.reset();
            return cached.copy();
        } else {
            ResourceService resourceService = AppBeans.get(ResourceService.NAME);
            String xml = resourceService.getResourceAsString(resourceName);
            if (xml != null) {
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();
                Element element = getElement(actionName, root);

                if (element != null) {
                    String activity = actionName;
                    String transition = null;

                    if ("activity".equals(element.getName())) {
                        activity = StringUtils.substringBeforeLast(actionName, ".");
                        transition = StringUtils.substringAfterLast(actionName, ".");
                    }

                    FormManagerChain managerChain = instantiateFormManagerChain();
                    managerChain.setCommonParams(getCommonParameters(element, actionName, activity, transition));

                    for (Element elem : Dom4j.elements(element)) {
                        FormManager manager = createFormManager(elem, activity, transition, managerChain);
                        if (manager != null) {
                            if (manager.isBefore()) {
                                managerChain.addManagerBefore(manager);
                            }
                            if (manager.isAfter()) {
                                managerChain.addManagerAfter(manager);
                            }
                        }
                    }

                    cache.put(cacheKey, managerChain);
                    return managerChain.copy();
                }
            }

            cache.put(cacheKey, nullObject);
            return nullObject;
        }
    }

    protected Element getElement(String actionName, Element root) {
        Element element;
        switch (actionName) {
            case WfConstants.ACTION_SAVE:
            case WfConstants.ACTION_SAVE_AND_CLOSE:
                element = root.element("save");
                break;
            case WfConstants.ACTION_START:
                element = root.element("start");
                break;
            case WfConstants.ACTION_CANCEL:
                element = root.element("cancel");
                break;
            case WfConstants.ACTION_REASSIGN:
                element = root.element("reassign");
                break;
            default:
                element = findActivityElement(actionName, root);
                break;
        }

        return element;
    }

    protected Element findActivityElement(String actionName, Element root) {
        String activity = StringUtils.substringBeforeLast(actionName, ".");
        String transition = StringUtils.substringAfterLast(actionName, ".");
        for (Element activityElem : Dom4j.elements(root, "activity")) {
            if (activity.equals(activityElem.attributeValue("name"))) {
                for (Element transitionElem : Dom4j.elements(activityElem, "transition")) {
                    if (transition.equals(transitionElem.attributeValue("name"))) {
                        return transitionElem;
                    }
                }
            }
        }

        return null;
    }

    protected FormManagerChain instantiateFormManagerChain() {
        return new FormManagerChain();
    }

    protected Map<String, Object> getCommonParameters(Element element,
                                                      String actionName,
                                                      String activity,
                                                      String transition) {
        Map<String, Object> commonParams = new HashMap<>();
        commonParams.put("activity", activity);
        commonParams.put("transition", transition);

        String style = StringUtils.trimToNull(element.attributeValue("style"));
        if (style == null && ("Ok".equals(transition) || "START_PROCESS_ACTION".equals(actionName))) {
            style = "wf-success";
        }
        if (style == null && ("NotOk".equals(transition) || "CANCEL_PROCESS_ACTION".equals(actionName))) {
            style = "wf-failure";
        }
        if (style != null) {
            commonParams.put("style", style);
        }
        return commonParams;
    }

    protected FormManager createFormManager(Element elem,
                                            String activity,
                                            String transition,
                                            FormManagerChain managerChain) {
        FormManager manager;
        String name = elem.getName();
        switch (name) {
            case "screen":
                manager = new FormManager.ScreenFormManager(elem, activity, transition, managerChain);
                break;
            case "confirm":
                manager = new FormManager.ConfirmFormManager(elem, activity, transition, managerChain);
                break;
            case "invoke":
                manager = new FormManager.ClassFormManager(elem, activity, transition, managerChain);
                break;
            default:
                manager = null;
                log.warn("Unknown form element: " + name);
        }
        return manager;
    }


}
