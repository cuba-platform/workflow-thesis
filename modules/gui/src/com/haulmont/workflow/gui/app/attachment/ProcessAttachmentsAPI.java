/*
 * Copyright (c) 2008-2015 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.app.attachment;

import com.haulmont.cuba.core.entity.Entity;
import com.haulmont.workflow.core.entity.Assignment;

import java.util.Collection;
import java.util.List;

/**
 * @author kovalenko
 * @version $Id$
 */
public interface ProcessAttachmentsAPI {

    String NAME = "workflow_ProcessAttachmentsManager";

    List<Entity> copyAttachments(Collection<Assignment> assignments);
}
