/*
* Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
* Haulmont Technology proprietary and confidential.
* Use is subject to license terms.

* Author: Konstantin Devyatkin
*
*
* $Id
*/
package com.haulmont.workflow.core.app;

import java.util.Map;


public interface NotificationMessage {
    String getSubject();
    String getBody();
    void setParameters(Map<String,Object> parameters);
}
