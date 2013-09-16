/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.app;


import com.haulmont.workflow.core.error.DesignCompilationError;

import java.io.Serializable;
import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public class CompilationMessage implements Serializable {

    private List<DesignCompilationError> errors;
    private List<String> warnings;

    public CompilationMessage(List<DesignCompilationError> errors, List<String> warnings) {
        this.errors = errors;
        this.warnings = warnings;
    }

    public List<DesignCompilationError> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
