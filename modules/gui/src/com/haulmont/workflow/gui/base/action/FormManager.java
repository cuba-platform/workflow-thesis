/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.base.action;

import com.haulmont.bali.util.Dom4j;
import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.cuba.core.app.DataService;
import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.cuba.core.global.*;
import com.haulmont.cuba.gui.WindowManager;
import com.haulmont.cuba.gui.WindowManager.OpenType;
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
import org.dom4j.dom.DOMElement;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author gorbunkov
 * @version $Id$
 */
public abstract class FormManager implements Serializable {

    protected Element element;
    protected String activity;
    protected String transition;
    protected boolean before;
    protected boolean after;
    protected String comment;

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
        Map<String, Object> params = new HashMap<>();
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
        DataService ds = AppBeans.get(DataService.NAME);
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

    public String getComment() {
        return comment == null ? "" : comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public FormManager copy() {
        FormManager copiedFormManager = null;
        try {
            Constructor<? extends FormManager> constructor = getClass().getConstructor(Element.class, String.class, String.class, FormManagerChain.class);
            copiedFormManager = constructor.newInstance(element, activity, transition, chain);
            copiedFormManager.setActivity(activity);
            copiedFormManager.setElement(element);
            copiedFormManager.setTransition(transition);
        } catch (Exception e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
        return copiedFormManager;
    }

    public static class ScreenFormManager extends FormManager {

        private String screenId;
        private Map<String, Object> params;

        protected WindowConfig windowConfig = AppBeans.get(WindowConfig.class);

        public ScreenFormManager(Element element, String activity, String transition, FormManagerChain chain) {
            super(element, activity, transition, chain);
            screenId = element.attributeValue("id");
            params = getScreenParams(element);
        }

        @Override
        public void doBefore(Map<String, Object> params) {
            params.put("before", true);
            params.put("chainComment", getComment());
            params.putAll(this.params);

            WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
            WindowManagerProvider wmp = AppBeans.get(WindowManagerProvider.NAME);

            Window window = wmp.get().openWindow(windowInfo, OpenType.DIALOG, params);
            window.addCloseListener(actionId -> {
                if (window instanceof WfForm) {
                    WfForm wfForm = (WfForm) window;
                    FormResult formResult = wfForm.getFormResult();
                    if (formResult != null) {
                        chain.addFormResult(screenId, formResult);
                    }
                }
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    String comment1 = window instanceof WfForm ? ((WfForm) window).getComment() : "";
                    try {
                        chain.doManagerBefore(comment1);
                    } catch (RuntimeException e) {
                        chain.fail();
                        throw e;
                    }
                } else {
                    chain.fail();
                }
            });
        }

        @Override
        public void doAfter(Map<String, Object> params) {
            params.put("after", true);
            params.putAll(this.params);

            WindowInfo windowInfo = windowConfig.getWindowInfo(screenId);
            WindowManagerProvider wmp = AppBeans.get(WindowManagerProvider.NAME);

            final Window window = wmp.get().openWindow(windowInfo, OpenType.DIALOG, params);
            window.addCloseListener(actionId -> {
                if (Window.COMMIT_ACTION_ID.equals(actionId)) {
                    chain.doManagerAfter();
                }
            });
        }
    }

    public static class ClassFormManager extends FormManager {

        private String className;
        private String script;
        private Map<String, Object> params;

        public ClassFormManager(Element element, String activity, String transition, FormManagerChain chain) {
            super(element, activity, transition, chain);
            className = element.attributeValue("class");
            Element scriptEl = element.element("script");
            if (scriptEl != null)
                script = scriptEl.getStringValue();
            params = getScreenParams(element);
        }

        private Callable<Boolean> getCallable(Map<String, Object> params) {
            Class cls = AppBeans.get(Scripting.class).loadClass(className);
            Callable<Boolean> runnable;
            try {
                runnable = (Callable<Boolean>) ReflectionHelper.newInstance(cls, params);
            } catch (NoSuchMethodException e) {
                try {
                    runnable = (Callable<Boolean>) cls.newInstance();
                } catch (InstantiationException | IllegalAccessException e1) {
                    throw new RuntimeException(e1);
                }
            }
            return runnable;
        }

        private Boolean call(Map<String, Object> params) throws Exception {
            if (className != null) {
                return getCallable(params).call();
            } else {
                return AppBeans.get(Scripting.class).evaluateGroovy(script, params);
            }
        }

        @Override
        public void doBefore(Map<String, Object> params) {
            params.put("before", true);
            params.putAll(this.params);
            try {
                Boolean result = call(params);
                if (!BooleanUtils.isFalse(result)) {
                    chain.doManagerBefore(getComment(), params);
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
            try {
                Boolean result = call(params);
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
            WindowManagerProvider wmp = AppBeans.get(WindowManagerProvider.NAME);
            Card card = (Card) chain.getCommonParams().get("card");
            Messages messages = AppBeans.get(Messages.NAME);
            wmp.get().showOptionDialog(
                    messages.getMessage(getClass(), "confirmationForm.title"),
                    messages.formatMessage(getClass(), "confirmationForm.msg",
                            messages.getMessage(card.getProc().getMessagesPack(), activity + "." + transition)),
                    Frame.MessageType.CONFIRMATION_HTML,
                    new Action[]{
                            new DialogAction(DialogAction.Type.YES) {
                                @Override
                                public void actionPerform(Component component) {
                                    chain.doManagerBefore(getComment());
                                }
                            },
                            new DialogAction(DialogAction.Type.NO) {
                                @Override
                                public void actionPerform(Component component) {
                                    chain.fail();
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

    public abstract static class SimpleFormManager extends FormManager {

        public SimpleFormManager(FormManagerChain chain) {
            super(new DOMElement(""), "", "", chain);
        }

        // provide your specific implementation
        public abstract FormManager copy();

        @Override
        public void doBefore(Map<String, Object> params) {
        }

        @Override
        public void doAfter(Map<String, Object> params) {
        }
    }
}
