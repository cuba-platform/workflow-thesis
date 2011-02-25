/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.12.10 11:06
 *
 * $Id$
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
window.onbeforeunload=(function(){
	if (!Wf.editor.isSaved()){
        if(window.confirm("Сохранить изменения?")){
            var value = Wf.editor.getValue();
            while(value.name === "") {
       	        value.name = prompt(i18nDict.ChooseName);
                if (value.name==null){
                    valee.name="Unnamed "+ new Date()
                }
            }
            value.working.properties.name=value.name;

		Wf.editor.tempSavedWiring = {name: value.name, working: value.working, language: Wf.editor.options.languageName };
        Wf.editor.adapter.saveWiring(Wf.editor.tempSavedWiring, {
       	    scope: Wf.editor
    	});
        }
    }
});

