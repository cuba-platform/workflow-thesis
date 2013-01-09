/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.gui.ServiceLocator;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.web.ui.base.action.CardContext;

import java.util.Map;

/**
 * @author subbotin
 * @version $Id$
 */
public class SubProcForm extends TransitionForm {

    public SubProcForm(IFrame frame) {
        super(frame);
    }

    public void init(Map<String, Object> params) {
        Card card = (Card) params.get("card");
        String subProcCode = (String) params.get("subProcCode");
        Card subProcCard = createSubProcCard(card, subProcCode);
        params.put("card", subProcCard);
        if (params.containsKey("subProcCard")) {
            CardContext subProcCardContext = (CardContext) params.get("subProcCard");
            subProcCardContext.setCard(subProcCard);
        }
        super.init(params);
    }

    protected Card createSubProcCard(Card parentCard, String subProcCode){
        WfService wfService = ServiceLocator.lookup(WfService.NAME);
        return wfService.createSubProcCard(parentCard, subProcCode);
    }
}








