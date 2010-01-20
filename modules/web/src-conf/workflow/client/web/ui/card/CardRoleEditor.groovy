/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 19.01.2010 15:43:09
 *
 * $Id$
 */
package workflow.client.web.ui.card

import com.haulmont.cuba.gui.components.AbstractEditor
import com.haulmont.cuba.gui.components.IFrame
import com.haulmont.cuba.core.entity.Entity
import com.haulmont.workflow.core.entity.ProcRole
import com.haulmont.cuba.gui.components.LookupField

class CardRoleEditor extends AbstractEditor{
  private ProcRole procRole

  CardRoleEditor(IFrame frame) {
    super(frame);
  }

  protected void init(Map<String, Object> params) {
    super.init(params);
    procRole = params['param$procRole']
  }

  void setItem(Entity item) {
    super.setItem(item);
    LookupField roleLookup = getComponent("roleLookup")
    roleLookup.setEnabled(false)
    roleLookup.value = procRole
  }


}
