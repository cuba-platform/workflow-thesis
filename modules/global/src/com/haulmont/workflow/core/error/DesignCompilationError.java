/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
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
