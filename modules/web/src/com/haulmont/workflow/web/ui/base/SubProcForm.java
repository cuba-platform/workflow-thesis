/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.workflow.web.ui.base;

import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.workflow.core.app.WfService;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.gui.base.action.CardContext;

import java.util.Map;

/**
 * @author subbotin
 * @version $Id$
 */
public class SubProcForm extends TransitionForm {

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
        WfService wfService = AppBeans.get(WfService.NAME);
        return wfService.createSubProcCard(parentCard, subProcCode);
    }
}