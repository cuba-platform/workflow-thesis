/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.workflow.core.entity.*;
import org.apache.commons.lang.BooleanUtils;

import org.springframework.stereotype.Component;
import javax.inject.Inject;
import java.util.*;

@Component(ProcessVariableAPI.NAME)
public class ProcessVariableManager implements ProcessVariableAPI {

    @Inject
    private WfEntityDescriptorTools wfEntityDescriptorTools;

    @Inject
    protected Metadata metadata;

    @Override
    public String getStringValue(Object value) {
        return wfEntityDescriptorTools.getStringValue(value);
    }

    @Override
    public Object getValue(AbstractProcessVariable designProcessVariable) {
        return wfEntityDescriptorTools.getValue(designProcessVariable);
    }

    @Override
    public String getLocalizedValue(AbstractProcessVariable designProcessVariable) {
        return wfEntityDescriptorTools.getLocalizedValue(designProcessVariable);
    }

    @Override
    public Map<String, CardVariable> getVariablesForCard(Card card) {

        Map<String, CardVariable> processVariableMap = new HashMap<>();
        for (CardVariable cardVariable : card.getCardVariables()) {
            processVariableMap.put(cardVariable.getAlias(), cardVariable);
        }
        return processVariableMap;
    }

    @Override
    public void createVariablesForCard(Card card) {
        EntityManager em = AppBeans.get(Persistence.class).getEntityManager();
        List<CardVariable> cardVariables = new ArrayList<>();
        Map<String, AbstractProcessVariable> variableMap = collectVariablesForCard(card);
        List<String> errors = checkVariables(variableMap.values());
        if (!errors.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String error : errors) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(error);
            }
            throw new IllegalStateException(sb.toString());
        }

        Map<String, AbstractProcessVariable> designVariables = getDesignVariables(card, new HashMap<>());

        for (String key : variableMap.keySet()) {
            CardVariable cardVariable = (CardVariable) variableMap.get(key).copyTo(metadata.create(CardVariable.class));
            cardVariable.setCard(card);
            if (designVariables.containsKey(key)) {
                DesignProcessVariable designProcessVariable = (DesignProcessVariable) designVariables.get(key);
                cardVariable.setModuleName(designProcessVariable.getModuleName());
                cardVariable.setPropertyName(designProcessVariable.getPropertyName());
            }
            em.persist(cardVariable);
            cardVariables.add(cardVariable);
        }
        card.setCardVariables(cardVariables);
    }

    @Override
    public List<String> checkVariables(Collection<AbstractProcessVariable> variablesForCard) {
        List<String> errorsList = new ArrayList<>();
        for (AbstractProcessVariable processVariable : variablesForCard) {
            if (processVariable instanceof DesignProcessVariable) {
                DesignProcessVariable designProcessVariable = (DesignProcessVariable) processVariable;
                if (BooleanUtils.isTrue(designProcessVariable.getShouldBeOverridden())) {
                    errorsList.add(String.format("Variable \"%s\" should be overridden", designProcessVariable.getName()));
                }
            }
        }
        return errorsList;
    }

    @Override
    public Map<String, AbstractProcessVariable> collectVariablesForCard(Card card) {
        Map<String, AbstractProcessVariable> processVariableMap = new HashMap<>();
        getDesignVariables(card, processVariableMap);
        getProcVariables(card, processVariableMap);
        return processVariableMap;
    }

    protected Map<String, AbstractProcessVariable> getDesignVariables(Card card, Map<String, AbstractProcessVariable> processVariableMap) {
        Design design = card.getProc().getDesign();
        if (design == null || design.getDesignProcessVariables() == null) return processVariableMap;
        for (DesignProcessVariable designProcessVariable : design.getDesignProcessVariables()) {
            processVariableMap.put(designProcessVariable.getAlias(), designProcessVariable);
        }
        return processVariableMap;
    }

    protected Map<String, AbstractProcessVariable> getProcVariables(Card card, Map<String, AbstractProcessVariable> processVariableMap) {
        if (card.getProc().getProcessVariables() == null) return processVariableMap;
        for (ProcVariable procVariable : card.getProc().getProcessVariables()) {
            processVariableMap.put(procVariable.getAlias(), procVariable);
        }
        return processVariableMap;
    }
}