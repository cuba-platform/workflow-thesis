/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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
