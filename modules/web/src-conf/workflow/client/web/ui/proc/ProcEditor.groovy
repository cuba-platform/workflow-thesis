/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.11.2009 19:01:55
 *
 * $Id$
 */
package workflow.client.web.ui.proc

import com.haulmont.cuba.gui.components.*
import com.haulmont.cuba.gui.data.Datasource
import com.haulmont.workflow.core.entity.Proc

public class ProcEditor extends AbstractEditor {

  public ProcEditor(IFrame frame) {
    super(frame);
  }

  @Override
  protected void init(Map<String, Object> params) {
    super.init(params)
    final Datasource<Proc> procDs = getDsContext().get("procDs")

    Table rolesTable = getComponent("rolesTable")
    TableActionsHelper helper = new TableActionsHelper(this, rolesTable)
    helper.createCreateAction([
            getParameters: {
              return Collections.emptyMap()
            },
            getValues: {
              Map<String, Object> values = new HashMap<String, Object>()
              values.put("proc", procDs.getItem())
              return values
            }
    ] as ValueProvider)
    helper.createEditAction()
    helper.createRemoveAction(false)
  }
}
