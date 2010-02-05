/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 04.02.2010 9:41:48
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.app.ResourceRepositoryService;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FormManagerChain {

    public interface Handler {
        void doSuccess(String comment);
    }

    private static FormManagerChain nullObject = new FormManagerChain();
    private static Map<String, FormManagerChain> cache = new ConcurrentHashMap<String, FormManagerChain>();

    public static FormManagerChain getManagerChain(Card card, String actionName) {
        String resourceName = card.getProc().getMessagesPack().replace(".", "/") + "/forms.xml";

        String cacheKey = resourceName + "/" + actionName;
        FormManagerChain cached = cache.get(cacheKey);
        if (cached != null) {
            cached.reset();
            return cached;
        } else {
            ResourceRepositoryService rr = ServiceLocator.lookup(ResourceRepositoryService.NAME);
            if (rr.resourceExists(resourceName)) {
                String xml = rr.getResAsString(resourceName);
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();

                Element element = null;
                String activity;
                String transition;
                if (WfConstants.ACTION_SAVE.equals(actionName)) {
                    activity = actionName;
                    transition = null;
                    element = root.element("save");
                } else if (WfConstants.ACTION_START.equals(actionName)) {
                    activity = actionName;
                    transition = null;
                    element = root.element("start");
                } else {
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
                }
                if (element != null) {
                    FormManagerChain managerChain = new FormManagerChain();

                    Map<String, Object> commonParams = new HashMap<String, Object>();
                    commonParams.put("activity", activity);
                    commonParams.put("transition", transition);

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
                    return managerChain;
                }
            }

            cache.put(cacheKey, nullObject);
            return nullObject;
        }
    }

    private Handler handler;

    //activity and transitions params fills when reading FormManagerChain from xml
    //card and assignmentId fills before usage
    private Map<String, Object> commonParams;

    private List<FormManager> managersBefore = new ArrayList<FormManager>();
    private int positionBefore = 0;

    private List<FormManager> managersAfter = new ArrayList<FormManager>();
    private int positionAfter = 0;

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
        FormManager nextManager = getNextManagerBefore();
        if (nextManager != null)
            nextManager.doBefore(commonParams);
        else {
            handler.doSuccess(comment);
        }
    }


    public void addManagerAfter(FormManager manager) {
        managersAfter.add(manager);
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

}
