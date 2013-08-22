package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.core.global.AssignmentInfo;

import java.util.Map;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public interface WfWorkerAPI {

    String NAME = "workflow_WfWorker";

    AssignmentInfo getAssignmentInfo(Card card);

    Map<String, Object> getProcessVariables(Card card);

    void setProcessVariables(Card card, Map<String, Object> variables);

    void setHasAttachmentsInCard(Card card, Boolean hasAttachments);
}
