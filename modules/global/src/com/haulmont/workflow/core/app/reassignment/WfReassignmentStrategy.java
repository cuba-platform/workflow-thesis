/*
 * Copyright (c) 2008-2020 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app.reassignment;

/**
 * @author stefanov
 */
public interface WfReassignmentStrategy{

    WfReassignmentPredicatesFactory getReassignmentPredicatesFactory();

}
