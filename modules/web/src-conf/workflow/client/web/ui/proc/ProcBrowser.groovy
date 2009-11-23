/*
 * Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 19.11.2009 19:29:44
 *
 * $Id$
 */
package workflow.client.web.ui.proc

import com.haulmont.cuba.gui.components.*

public class ProcBrowser extends AbstractWindow {

  public ProcBrowser(IFrame frame) {
    super(frame)
  }

  @Override
  protected void init(Map<String, Object> params) {
    super.init(params)
    Table table = getComponent("procTable")
    TableActionsHelper helper = new TableActionsHelper(this, table)
    helper.createEditAction()
  }
}
