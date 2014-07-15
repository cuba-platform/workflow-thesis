/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.gui.sys;

import com.haulmont.cuba.client.sys.MessagesClientImpl;
import com.haulmont.cuba.core.global.Configuration;

import javax.inject.Inject;

/**
 * {@link com.haulmont.cuba.core.global.Messages} implementation that always sets remoteSearch flag to true
 * regardles of cuba.remoteMessagesSearchEnabled application property.
 *
 * @author krivopustov
 * @version $Id$
 */
public class WfMessagesClientImpl extends MessagesClientImpl {

    @Inject
    @Override
    public void setConfiguration(Configuration configuration) {
        super.setConfiguration(configuration);
        remoteSearch = true;
    }

    @Override
    public void setRemoteSearch(boolean remoteSearch) {
        this.remoteSearch = remoteSearch;
    }
}
