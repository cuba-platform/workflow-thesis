/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Assignment;
import com.haulmont.workflow.core.entity.CardRole;

import javax.annotation.ManagedBean;
import java.util.Date;

/**
 * @author d.evdokimov
 * @version $Id$
 */
@ManagedBean(DateHelperBean.NAME)
public class DateHelperBean {

    public static final String NAME = "wf_DateHelperBean";

    public Date prepareAssignmentDueDate(Assignment assignment, CardRole cardRole, Date assignmentDueDate) {
        return assignmentDueDate;
    }
}