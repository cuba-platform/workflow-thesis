/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;

import com.google.common.collect.Lists;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.MessageTools;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.global.WfConfig;
import com.haulmont.workflow.core.global.WfConstants;
import org.apache.commons.lang.StringUtils;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

/**
 * @author chekashkin
 * @version $Id$
 */

@ManagedBean(AssignmentLocalizationTools.NAME)
public class AssignmentLocalizationToolsImpl implements AssignmentLocalizationTools {
    @Inject
    protected Configuration configuration;
    @Inject
    protected Messages messages;

    protected List<String> systemAssignmentOutcomes;

    @PostConstruct
    public void init() {
        String systemOutcomesParam = configuration.getConfig(WfConfig.class).getSystemAssignmentOutcomes();
        if (StringUtils.isBlank(systemOutcomesParam))
            systemAssignmentOutcomes = Lists.newArrayList();
        else
            systemAssignmentOutcomes = Lists.newArrayList(systemOutcomesParam.split(","));
    }

    public String getLocalizedAttribute(Assignment assignment, String value) {
        if (value == null)
            return "";

        if (assignment.getProc() != null) {
            String messagesPack = assignment.getProc().getMessagesPack();
            if (!value.startsWith(MessageTools.MARK))
                value = MessageTools.MARK + value;
            return messages.getTools().loadString(messagesPack, value);
        }
        return value;
    }

    @Override
    public String getLocOutcomeResult(Assignment assignment) {
        if (assignment.getOutcome() == null)
            return null;
        else {
            // If outcome is system localization will be searching in main message pack by key - outcome + "Result"
            // e.g if outcome is "Saved" then localization key must be "SavedResult"
            if (isSystemOutcome(assignment.getOutcome()))
                return messages.getMainMessage(assignment.getOutcome() + "Result");

            if (WfConstants.ACTION_REASSIGN.equals(assignment.getOutcome())) {
                String key = WfConstants.ACTION_REASSIGN + ".Result";
                String locValue = getLocalizedAttribute(assignment, key);
                if (key.equals(locValue)) {
                    return messages.getMessage(Assignment.class, key);
                } else {
                    return locValue;
                }
            }
            String key = assignment.getName() + "." + assignment.getOutcome() + ".Result";
            String value = getLocalizedAttribute(assignment, key);
            if (key.equals(value)) {
                key = assignment.getName() + "." + assignment.getOutcome() + "Result"; // try old style
                return getLocalizedAttribute(assignment, key);
            } else
                return value;
        }
    }

    @Override
    public String getLocOutcome(Assignment assignment) {
        if (isSystemOutcome(assignment.getOutcome()))
            return messages.getMainMessage(assignment.getOutcome());

        return assignment.getOutcome() == null ? null :
                getLocalizedAttribute(assignment, assignment.getName() + "." + assignment.getOutcome());
    }

    /**
     * Check if assignment outcome is system i.e contained in workflow.systemAssignmentOutcomes property
     * For system assignments localization main message pack is used
     */
    protected boolean isSystemOutcome(String outcome) {
        return systemAssignmentOutcomes.contains(outcome);
    }
}
