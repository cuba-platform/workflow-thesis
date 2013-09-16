/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Assignment;

import java.util.UUID;

/**
 * @author pavlov
 * @version $Id$
 */
public class AssignmentCollectionDatasource extends CollectionDatasourceImpl<Assignment,UUID> {

    @Override
    public void setItem(Assignment item) {
        super.setItem(item);

        attachListener(item);
    }
}
