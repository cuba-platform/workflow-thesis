/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Maxim Gorbunkov
 * Created: 18.11.2010 14:07:37
 *
 * $Id$
 */
package com.haulmont.workflow.core.app;

import com.haulmont.workflow.core.entity.Card;

public class WfUtils {

    /**
     * Checks whether card's state field contains sended state
     * @param card
     * @param state
     * @return
     */
    public static boolean isCardInState(Card card, String state) {
        String currentState = card.getState();
        if (currentState == null) {
            if (state == null)
                return true;
            else
                return false;
        }

        return currentState.contains("," + state + ",");
    }

    /**
     * Checks whether card's state field contains one or more state from states param
     * @param card
     * @param states
     * @return
     */
    public static boolean isCardInStateList(Card card, String... states) {
        for (String state : states) {
            if (isCardInState(card, state)) return true;
        }
        return false;
    }
}
