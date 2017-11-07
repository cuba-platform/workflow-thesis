/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package com.haulmont.workflow.gui.app.base;

import com.haulmont.cuba.gui.data.impl.CollectionDatasourceImpl;
import com.haulmont.workflow.core.entity.Assignment;

import java.util.UUID;

public class AssignmentCollectionDatasource extends CollectionDatasourceImpl<Assignment,UUID> {

    @Override
    public void setItem(Assignment item) {
        super.setItem(item);

        attachListener(item);
    }
}
