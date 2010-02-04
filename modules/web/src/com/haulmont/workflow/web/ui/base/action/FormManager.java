/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.01.2010 10:30:55
 *
 * $Id$
 */
package com.haulmont.workflow.web.ui.base.action;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.app.ResourceRepositoryService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.gui.AppConfig;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.WebWindowManager;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public abstract class FormManager {

    public interface Handler {
        void commit(String comment);
    }

    protected Element element;
    protected String activity;
    protected String transition;
    protected boolean before;
    protected boolean after;

    protected Log log = LogFactory.getLog(getClass());

    protected FormManager(Element element, String activity, String transition) {
        this.element = element;
        this.activity = activity;
        this.transition = transition;

        before = Boolean.valueOf(element.attributeValue("before"));
        after = Boolean.valueOf(element.attributeValue("after"));
        if (!before && !after)
            before = true;
    }

    public boolean isAfter() {
        return after;
    }

    public boolean isBefore() {
        return before;
    }

    protected Map<String, Object> makeCommonParams(Card card, UUID assignmentId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("card", card);
        params.put("assignmentId", assignmentId);
        params.put("activity", activity);
        params.put("transition", transition);
        return params;
    }

    protected Map<String, Object> getScreenParams(Element element) {
        Map<String, Object> params = new HashMap<String, Object>();
        for (Element paramElem : Dom4j.elements(element, "param")) {
            String value = paramElem.attributeValue("value");
            EntityLoadInfo info = EntityLoadInfo.parse(value);
            if (info == null)
                params.put(paramElem.attributeValue("name"), value);
            else
                params.put(paramElem.attributeValue("name"), loadEntityInstance(info));
        }
        return params;
    }

    protected Entity loadEntityInstance(EntityLoadInfo info) {
        DataService ds = ServiceLocator.getDataService();
        LoadContext ctx = new LoadContext(info.getMetaClass()).setId(info.getId());
        if (info.getViewName() != null)
            ctx.setView(info.getViewName());
        Entity entity = ds.load(ctx);
        return entity;
    }

    public abstract void doBefore(final FormManagerChain chain, Map<String, Object> params);

    public abstract void doAfter(Card card, UUID assignmentId);

    public static class ScreenFormManager extends FormManager {

        private String screenId;
        private Map<String, Object> params;

        public ScreenFormManager(Element element, String activity, String transition) {
            super(element, activity, transition);
            screenId = element.attributeValue("id");
            params = getScreenParams(element);
        }

        @Override
        public void doBefore(final FormManagerChain chain, Map<String, Object> params) {
            params.put("before", true);
            params.putAll(this.params);

            WindowInfo windowInfo = AppConfig.getInstance().getWindowConfig().getWindowInfo(screenId);
            WebWindowManager windowManager = App.getInstance().getWindowManager();

            final Window window = windowManager.openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
            window.addListener(new Window.CloseListener() {
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        String comment = window instanceof AbstractForm ?
                                ((AbstractForm) window).getComment() : "";
                        chain.doManagerBefore(comment);
                    }
                }
            });
        }

        @Override
        public void doAfter(Card card, UUID assignmentId) {
            Map<String, Object> params = makeCommonParams(card, assignmentId);
            params.put("after", true);
            params.putAll(this.params);

            WindowInfo windowInfo = AppConfig.getInstance().getWindowConfig().getWindowInfo(screenId);
            WebWindowManager windowManager = App.getInstance().getWindowManager();

            windowManager.openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
        }
    }

    public static class ClassFormManager extends FormManager {

        private String className;
        private Map<String, Object> params;

        public ClassFormManager(Element element, String activity, String transition) {
            super(element, activity, transition);
            className = element.attributeValue("class");
            params = getScreenParams(element);
        }

        private Callable<Boolean> getCallable(Map<String, Object> params) {
            Class cls = ScriptingProvider.loadClass(className);
            Callable<Boolean> runnable;
            try {
                runnable = (Callable<Boolean>) ReflectionHelper.newInstance(cls, params);
            } catch (NoSuchMethodException e) {
                try {
                    runnable = (Callable<Boolean>) cls.newInstance();
                } catch (InstantiationException e1) {
                    throw new RuntimeException(e1);
                } catch (IllegalAccessException e1) {
                    throw new RuntimeException(e1);
                }
            }
            return runnable;
        }

        @Override
        public void doBefore(final FormManagerChain chain, Map<String, Object> params) {
            params.put("before", true);
            params.putAll(this.params);

            Callable<Boolean> runnable = getCallable(params);
            try {
                Boolean result = runnable.call();
                if (!BooleanUtils.isFalse(result)) {
                    chain.doManagerBefore("");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void doAfter(Card card, UUID assignmentId) {
            Map<String, Object> params = makeCommonParams(card, assignmentId);
            params.put("after", true);
            params.putAll(this.params);

            Callable<Boolean> runnable = getCallable(params);
            try {
                runnable.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ConfirmFormManager extends FormManager {

        public ConfirmFormManager(Element element, String activity, String transition) {
            super(element, activity, transition);
        }

        @Override
        public void doBefore(final FormManagerChain chain, Map<String, Object> params) {
            WebWindowManager wm = App.getInstance().getWindowManager();
            Card card = (Card)chain.getCommonParams().get("card");
            wm.showOptionDialog(
                    MessageProvider.getMessage(getClass(), "confirmationForm.title"),
                    MessageProvider.formatMessage(getClass(), "confirmationForm.msg",
                            MessageProvider.getMessage(card.getProc().getMessagesPack(), activity + "." + transition)),
                    IFrame.MessageType.CONFIRMATION,
                    new Action[] {
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    chain.doManagerBefore("");
                                }

                                @Override
                                public String getIcon() {
                                    return "icons/ok.png";
                                }
                            },
                            new DialogAction(DialogAction.Type.NO) {
                                @Override
                                public String getIcon() {
                                    return "icons/cancel.png";
                                }
                            }
                    }
            );
        }

        @Override
        public void doAfter(Card card, UUID assignmentId) {
            log.warn("Confirm form doesn't make sense 'after'");
        }
    }
}
