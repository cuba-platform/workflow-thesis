package com.haulmont.workflow.web.ui.cardvariables.browse;

import com.haulmont.cuba.gui.components.Action;
import com.haulmont.cuba.gui.components.IFrame;
import com.haulmont.workflow.core.entity.Card;
import com.haulmont.workflow.web.ui.designprocessvariables.browse.AbstractProcVariableBrowser;

import java.util.Collections;
import java.util.Map;

/**
 * <p>$Id: CardVariableBrowser.java 10533 2013-02-12 08:55:55Z zaharchenko $</p>
 *
 * @author Zaharchenko
 */
public class CardVariableBrowser extends AbstractProcVariableBrowser {

    private Card card;

    private static final long serialVersionUID = 4880567976812400606L;

    public CardVariableBrowser() {
        super();
    }

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);
        card = (Card) params.get("card");
        Action createAction = table.getAction("create");
        createAction.setEnabled(false);
    }

    @Override
    protected Map<String, Object> getInitialValuesForCreate() {
        return Collections.<String, Object>singletonMap("card", card);
    }
}
