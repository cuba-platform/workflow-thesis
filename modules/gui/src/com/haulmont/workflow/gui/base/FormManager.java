/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.01.2010 10:30:55
 *
 * $Id$
 */
package com.haulmont.workflow.gui.base;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.EntityLoadInfo;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.MessageProvider;
import com.haulmont.cuba.core.global.ScriptingProvider;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManagerProvider;
import com.haulmont.cuba.gui.components.*;
import com.haulmont.cuba.gui.config.WindowConfig;
import com.haulmont.cuba.gui.config.WindowInfo;
import com.haulmont.workflow.core.entity.Card;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public abstract class FormManager implements Serializable {

    protected Element element;
    protected String activity;
    protected String transition;
    protected boolean before;
    protected boolean after;

    protected FormManagerChain chain;

    protected transient Log log = LogFactory.getLog(getClass());

    protected FormManager(Element element, String activity, String transition, FormManagerChain chain) {
        this.element = element;
        this.activity = activity;
        this.transition = transition;
        this.chain = chain;

        before = Boolean.valueOf(element.attributeValue("before"));
        after = Boolean.valueOf(element.attributeValue("after"));
        if (!before && !after)
            before = true;
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        log = LogFactory.getLog(getClass());
    }

    public boolean isAfter() {
        return after;
    }

    public boolean isBefore() {
        return before;
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

    public void setFormManagerChain(FormManagerChain chain) {
        this.chain = chain;
    }

    public Element getElement() {
        return element;
    }

    public void setElement(Element element) {
        this.element = element;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getTransition() {
        return transition;
    }

    public void setTransition(String transition) {
        this.transition = transition;
    }

    public abstract void doBefore(Map<String, Object> params);

    public abstract void doAfter(Map<String, Object> params);

    public FormManager clone() {
        FormManager clonedFormManager = null;
        try {
            Constructor<? extends FormManager> constructor = getClass().getConstructor(Element.class, String.class, String.class, FormManagerChain.class);
            clonedFormManager = constructor.newInstance(element, activity, transition, chain);
            clonedFormManager.setActivity(activity);
            clonedFormManager.setElement(element);
            clonedFormManager.setTransition(transition);
        } catch (Exception e) {
           log.error(ExceptionUtils.getStackTrace(e));
        }
        return clonedFormManager;
    }

    public static class ScreenFormManager extends FormManager {

        private String screenId;
        private Map<String, Object> params;

        protected WindowConfig windowConfig = AppContext.getBean(WindowConfig.class);

        public ScreenFormManager(Element element, String activity, String transition, FormManagerChain chain) {
            super(element, activity, transition, chain);
            screenId = element.attributeValue("id");
            params = getScreenParams(element);
        }

        @Override
        public void doBefore(Map<String, Object> params) {
            params.put("before", true);
            params.putAll(this.params);

            WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
            WindowManagerProvider wmp = ServiceLocator.lookup(WindowManagerProvider.NAME);

            final Window window = wmp.get().openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
            window.addListener(new Window.CloseListener() {
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        String comment = window instanceof WfForm ?
                                ((WfForm) window).getComment() : "";
                        try {
                            chain.doManagerBefore(comment);
                        } catch (RuntimeException e) {
                            chain.fail();
                            throw e;
                        }
                    } else {
                        chain.fail();
                    }
                }
            });
        }

        @Override
        public void doAfter(Map<String, Object> params) {
            params.put("after", true);
            params.putAll(this.params);

            WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
            WindowManagerProvider wmp = ServiceLocator.lookup(WindowManagerProvider.NAME);

            final Window window = wmp.get().openWindow(windowInfo, WindowManager.OpenType.DIALOG, params);
            window.addListener(new Window.CloseListener() {
                public void windowClosed(String actionId) {
                    if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                        chain.doManagerAfter();
                    }
                }
            });
        }
    }

    public static class ClassFormManager extends FormManager {

        private String className;
        private Map<String, Object> params;

        public ClassFormManager(Element element, String activity, String transition, FormManagerChain chain) {
            super(element, activity, transition, chain);
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
        public void doBefore(Map<String, Object> params) {
            params.put("before", true);
            params.putAll(this.params);

            Callable<Boolean> runnable = getCallable(params);
            try {
                Boolean result = runnable.call();
                if (!BooleanUtils.isFalse(result)) {
                    chain.doManagerBefore("", params);
                } else {
                    chain.fail();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void doAfter(Map<String, Object> params) {
            params.put("after", true);
            params.putAll(this.params);

            Callable<Boolean> runnable = getCallable(params);
            try {
                Boolean result = runnable.call();
                if (!BooleanUtils.isFalse(result)) {
                    chain.doManagerAfter();
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class ConfirmFormManager extends FormManager {

        public ConfirmFormManager(Element element, String activity, String transition, FormManagerChain chain) {
            super(element, activity, transition, chain);
        }

        @Override
        public void doBefore(Map<String, Object> params) {
            WindowManagerProvider wmp = ServiceLocator.lookup(WindowManagerProvider.NAME);
            Card card = (Card)chain.getCommonParams().get("card");
            wmp.get().showOptionDialog(
                    MessageProvider.getMessage(getClass(), "confirmationForm.title"),
                    MessageProvider.formatMessage(getClass(), "confirmationForm.msg",
                            MessageProvider.getMessage(card.getProc().getMessagesPack(), activity + "." + transition)),
                    IFrame.MessageType.CONFIRMATION,
                    new Action[]{
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
                                public void actionPerform(Component component) {
                                    chain.fail();
                                }

                                @Override
                                public String getIcon() {
                                    return "icons/cancel.png";
                                }
                            }
                    }
            );
        }

        @Override
        public void doAfter(Map<String, Object> params) {
            log.warn("Confirm form doesn't make sense 'after'");
        }
    }
}
