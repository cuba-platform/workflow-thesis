/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.01.2010 10:30:55
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.cuba.core.app.ResourceRepositoryService;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.Window;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FormManager {

    private String screenId;
    private Card card;
    private AssignmentInfo info;
    private String activity;
    private String transition;
    private String comment;

    private static FormManager nullObject = new FormManager();
    
    private static Map<String, FormManager> cache = new ConcurrentHashMap<String, FormManager>();

    public static FormManager create(Card card, AssignmentInfo info, String comment, String actionName) {
        String resourceName = card.getProc().getMessagesPack().replace(".", "/") + "/forms.xml";

        String cacheKey = resourceName + "/" + actionName;
        FormManager instance = cache.get(cacheKey);
        if (instance != null) {
            return instance == nullObject ? null : instance;
        } else {
            ResourceRepositoryService rr = ServiceLocator.lookup(ResourceRepositoryService.NAME);
            if (rr.resourceExists(resourceName)) {
                String xml = rr.getResAsString(resourceName);
                Document doc = Dom4j.readDocument(xml);
                Element root = doc.getRootElement();

                int dot = actionName.lastIndexOf('.');
                String activity = actionName.substring(0, dot);
                String transition = actionName.substring(actionName.lastIndexOf('.') + 1);

                for (Element activityElem : Dom4j.elements(root, "activity")) {
                    if (activity.equals(activityElem.attributeValue("name"))) {
                        for (Element transitionElem : Dom4j.elements(activityElem, "transition")) {
                            if (transition.equals(transitionElem.attributeValue("name"))) {
                                Element screenElem = transitionElem.element("screen");
                                if (screenElem != null && screenElem.attributeValue("id") != null) {
                                    FormManager processScreenManager = new FormManager(card, info, activity, transition, comment,
                                            screenElem.attributeValue("id"));
                                    cache.put(cacheKey, processScreenManager);
                                    return processScreenManager;
                                }
                            }
                        }
                    }
                }
            }
            cache.put(cacheKey, nullObject);
            return null;
        }
    }

    private FormManager() {
    }

    private FormManager(Card card, AssignmentInfo info, String activity, String transition,
                                 String comment, String screenId) {
        this.card = card;
        this.info = info;
        this.comment = comment;
        this.activity = activity;
        this.transition = transition;
        this.screenId = screenId;
    }

    public Window show() {
        WindowInfo windowInfo = AppConfig.getInstance().getWindowConfig().getWindowInfo(screenId);
        WebWindowManager windowManager = App.getInstance().getWindowManager();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("card", card);
        params.put("activity", activity);
        params.put("transition", transition);
        params.put("comment", comment);

        Window window = windowManager.openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
        return window;
    }

}
