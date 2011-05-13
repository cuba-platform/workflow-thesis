/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 01.02.11 14:51
 *
 * $Id$
 */
Wf.ScriptSelect = function(options) {
    Wf.ScriptSelect.superclass.constructor.call(this, options);

    this.refresh(false);
};

YAHOO.lang.extend(Wf.ScriptSelect, inputEx.SelectField, {

    setOptions: function(options) {
        Wf.OutputSelect.superclass.setOptions.call(this, options);

        this.options.className = options.className || 'Wf-ScriptSelect';
        this.options.container = options.container;
    },

    setValue: function(val, sendUpdatedEvt) {
        if (!val)
            return;
        this.refresh(true);
        Wf.ScriptSelect.superclass.setValue.call(this, val, sendUpdatedEvt);
    },

    refresh: function(useScriptsCache) {
        var i;

        this.clear();
        for (i = 0; i < this.choicesList.length; i++) {
            var choice = this.choicesList[i];
            this.removeChoiceNode(choice.node);
        }
        this.choicesList = [];

        if (useScriptsCache && Wf.scriptsCache) {
            for (var j = 0; j < Wf.scriptsCache.length; j++) {
                var s = Wf.scriptsCache[j];
                var v = {value: s};
                if (this.getChoicePosition(v) == -1)
                    this.addChoice(v);
            }
        } else {
            Wf.loadScripts(this.addScripts, this);
        }
    },

    addScripts: function(scripts) {
        Wf.scriptsCache = scripts;
        for (var i = 0; i < scripts.length; i++) {
            var v = {value: scripts[i]};
            if (this.getChoicePosition(v) == -1)
                this.addChoice(v);
        }
    }
});

inputEx.registerType("wfScriptSelect", Wf.ScriptSelect);
