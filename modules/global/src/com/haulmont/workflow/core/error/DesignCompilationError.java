/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

package com.haulmont.workflow.core.error;

import java.io.Serializable;

/**
 * <p>$Id$</p>
 *
 * @author devyatkin
 */
public interface DesignCompilationError extends Serializable{
    String getMessage();
}
