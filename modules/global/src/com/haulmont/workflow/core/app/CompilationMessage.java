/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.core.app;


import java.io.Serializable;
import java.util.List;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public class CompilationMessage implements Serializable {

    private List<String> errors;
    private List<String> warnings;

    public CompilationMessage(List<String> errors, List<String> warnings) {
        this.errors = errors;
        this.warnings = warnings;
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }
}
