/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

// InputEx needs a correct path to this image
//inputEx.spacerUrl = "lib/wireit/plugins/inputex/lib/inputex/images/space.gif";

var i18n = new I18n(i18nDict);

YAHOO.util.Event.onDOMReady(function() {
    try {
        Wf.registerForms();

        Wf.localizeLanguage(wfLanguage);

        wfLanguage.adapter = WireIt.WiringEditor.adapters.WfAdapter;

        Wf.editor = new Wf.Editor(wfLanguage);
        Wf.editor.accordionView.openPanel(1);

    } catch(ex) {
        console.log(ex);
    }
});

window.onload = (function() {
    i18n.translateNodes();
});

