/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */
package com.haulmont.workflow.core.app.design;

import com.haulmont.workflow.core.entity.Design;
import com.haulmont.workflow.core.entity.DesignFile;
import com.haulmont.workflow.core.entity.Proc;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DeployPostProcessor {

    public void doAfterDeploy(Design design, File dir, List<DesignFile> files, Proc proc) throws IOException {
    }
}
