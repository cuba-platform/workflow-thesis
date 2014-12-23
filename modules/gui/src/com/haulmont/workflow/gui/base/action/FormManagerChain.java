/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.base.action;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.app.ResourceService;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class FormManagerChain {

    public interface Handler {
        void onSuccess(String comment);

        void onFail();
    }

    private static FormManagerChain nullObject = new FormManagerChain();
    private static Map<String, FormManagerChain> cache = new ConcurrentHashMap<>();

    private Handler handler;

    //activity and transitions params fills when reading FormManagerChain from xml
    //card and assignmentId fills before usage
    private Map<String, Object> commonParams = new HashMap<>();

    private List<FormManager> managersBefore = new ArrayList<>();
    private int positionBefore = 0;

    private List<FormManager> managersAfter = new ArrayList<>();
    private int positionAfter = 0;

    // Results from forms after manager chain working. Key is a screen id.
    protected Map<String, FormResult> formResults = new HashMap<>();

    public static FormManagerChain getManagerChain(Card card, String actionName) {
        if (card.getProc() == null)
            return nullObject;

        String resourceName = card.getProc().getMessagesPack().replace(".", "/") + "/forms.xml";

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

                Element element = null;
                String activity = actionName;
                String transition = null;
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
                        int dot = actionName.lastIndexOf('.');
                        activity = actionName.substring(0, dot);
                        transition = actionName.substring(actionName.lastIndexOf('.') + 1);

                        for (Element activityElem : Dom4j.elements(root, "activity")) {
                            if (activity.equals(activityElem.attributeValue("name"))) {
                                for (Element transitionElem : Dom4j.elements(activityElem, "transition")) {
                                    if (transition.equals(transitionElem.attributeValue("name"))) {
                                        element = transitionElem;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                }
                if (element != null) {
                    FormManagerChain managerChain = new FormManagerChain();

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

                    managerChain.setCommonParams(commonParams);

                    for (Element elem : Dom4j.elements(element)) {
                        FormManager manager;
                        if (elem.getName().equals("screen")) {
                            manager = new FormManager.ScreenFormManager(elem, activity, transition, managerChain);
                        } else if (elem.getName().equals("confirm")) {
                            manager = new FormManager.ConfirmFormManager(elem, activity, transition, managerChain);
                        } else if (elem.getName().equals("invoke")) {
                            manager = new FormManager.ClassFormManager(elem, activity, transition, managerChain);
                        } else
                            throw new UnsupportedOperationException("Unknown form element: " + elem.getName());

                        if (manager.isBefore()) {
                            managerChain.addManagerBefore(manager);
                        }
                        if (manager.isAfter()) {
                            managerChain.addManagerAfter(manager);
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

    public FormManagerChain copy() {
        FormManagerChain copiedChain = new FormManagerChain();
        copiedChain.setCommonParams(new HashMap(getCommonParams()));

        for (FormManager manager : getManagersAfter()) {
            FormManager clonedManager = manager.copy();
            clonedManager.setFormManagerChain(copiedChain);
            copiedChain.addManagerAfter(clonedManager);
        }
        for (FormManager manager : getManagersBefore()) {
            FormManager clonedManager = manager.copy();
            clonedManager.setFormManagerChain(copiedChain);
            copiedChain.addManagerBefore(clonedManager);
        }

        return copiedChain;
    }

    public void addManagerBefore(FormManager manager) {
        managersBefore.add(manager);
    }

    private FormManager getNextManagerBefore() {
        if (positionBefore < managersBefore.size()) {
            return managersBefore.get(positionBefore++);
        } else return null;
    }

    public boolean hasManagersBefore() {
        return !managersBefore.isEmpty();
    }

    public void doManagerBefore(String comment) {
        doManagerBefore(comment, new HashMap<String, Object>());
    }

    public void doManagerBefore(String comment, Map<String, Object> params) {
        FormManager nextManager = getNextManagerBefore();
        if (nextManager != null) {
            params.putAll(commonParams);
            nextManager.setComment(comment);
            nextManager.doBefore(params);
        } else {
            handler.onSuccess(comment);
        }
    }

    public void addManagerAfter(FormManager manager) {
        managersAfter.add(manager);
    }

    public void addFormResult(String key, FormResult value) {
        formResults.put(key, value);
    }

    @SuppressWarnings("unused")
    public Map<String, FormResult> getFormResults() {
        return formResults;
    }

    @SuppressWarnings("unused")
    public FormResult getFormResult(String id) {
        return formResults.get(id);
    }

    private FormManager getNextManagerAfter() {
        if (positionAfter < managersAfter.size()) {
            return managersAfter.get(positionAfter++);
        } else return null;
    }

    public boolean hasManagersAfter() {
        return !managersAfter.isEmpty();
    }

    public void doManagerAfter() {
        FormManager nextManager = getNextManagerAfter();
        if (nextManager != null)
            nextManager.doAfter(commonParams);
    }

    public void doManagerAfter(Map<String, Object> params) {
        FormManager nextManager = getNextManagerAfter();
        if (nextManager != null) {
            params.putAll(commonParams);
            nextManager.doAfter(params);
        }
    }

    public void fail() {
        handler.onFail();
    }

    private void reset() {
        positionAfter = 0;
        positionBefore = 0;
    }

    public void setCommonParams(Map<String, Object> commonParams) {
        this.commonParams = commonParams;
    }

    public Map<String, Object> getCommonParams() {
        return commonParams;
    }

    public void setCard(Card card) {
        this.commonParams.put("card", card);
    }

    public void setAssignmentId(UUID assignmentId) {
        this.commonParams.put("assignmentId", assignmentId);
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public List<FormManager> getManagersBefore() {
        return managersBefore;
    }

    public void setManagersBefore(List<FormManager> managersBefore) {
        this.managersBefore = managersBefore;
    }

    public List<FormManager> getManagersAfter() {
        return managersAfter;
    }

    public void setManagersAfter(List<FormManager> managersAfter) {
        this.managersAfter = managersAfter;
    }
}
