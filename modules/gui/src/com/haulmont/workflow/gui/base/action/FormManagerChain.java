/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */
package com.haulmont.workflow.gui.base.action;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.gui.app.form.FormManagerChainBuilder;

import java.util.*;

/**
 * @author gorbunkov
 * @version $Id$
 */
public class FormManagerChain {

    public interface Handler {
        void onSuccess(String comment);

        void onFail();
    }

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
        return AppBeans.get(FormManagerChainBuilder.class).build(card, actionName);
    }

    public FormManagerChain copy() {
        FormManagerChain copiedChain = new FormManagerChain();
        copiedChain.setCommonParams(new HashMap<>(getCommonParams()));

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

    public void reset() {
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
