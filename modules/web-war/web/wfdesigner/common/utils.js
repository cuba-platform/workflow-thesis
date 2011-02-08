/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.12.10 17:14
 *
 * $Id$
 */

WireIt.Terminal.prototype.wireConfig = {
    xtype: "WireIt.BezierArrowWire"
};

WireIt.Terminal.prototype.editingWireConfig = WireIt.Terminal.prototype.wireConfig;

var Wf = {

    createProcIdParam: function() {
            var s = window.location.search;
            if (s && s[0] == "?") {
                var re = new RegExp("[\\?&]id=([^&#]*)");
                var res = re.exec(s);
                if (!res || !res[1])
                    return "";
                else
                    return "?id=" + res[1];
            }
    },

    processWiringOnLoad: function (wirings) {
//        for (var i in wirings) {
//            var wiring = wirings[i];
//            for (var j in wiring.working.wires) {
//                var wire = wiring.working.wires[j];
//                wire.labelEditor = {type: 'string', value: wire.label};
//            }
//        }
    },

    localizeLanguage: function (obj) {
        for (var prop in obj) {
            var val = obj[prop];
            if (val) {
                if (typeof(val) == "string") {
                    if (val.substr(0, 6) == "msg://") {
                        obj[prop] = i18n.get(val.substr(6));
                    }
                } else if (typeof(val) == "object") {
                    Wf.localizeLanguage(val);
                }
            }
        }
    },

    mergeLanguages: function(lang1, lang2) {
        var json = YAHOO.lang.JSON.stringify(lang1);
        var res = YAHOO.lang.JSON.parse(json);

        if (lang2.languageName)
            res.languageName = lang2.languageName;

        if (lang2.modules) {
            for (var i = 0; i < lang2.modules.length; i++) {
                var m = lang2.modules[i];

                var found = false;
                for (var j = 0; j < res.modules.length; j++) {
                    if (res.modules[j].name == m.name) {
                        res.modules[j] = m;
                        found = true;
                        break;
                    }
                }
                if (!found)
                    res.modules.push(m);
            }
        }

        return res;
    },


    loadScripts: function(callback, scope) {
        YAHOO.util.Connect.asyncRequest(
                'GET',
                'action/loadScripts.json' + Wf.createProcIdParam(),
                {
                    success: function(o) {
                        var r = YAHOO.lang.JSON.parse(o.responseText);
                        callback.call(scope, r);
                    },
                    failure: function(o) {
                        var error = o.status + " " + o.statusText;
                        console.log(error);
                    }
                },
                null
        );
    },

    initTerminalLabels: function(container, terminalConfigs) {
        for (var i = 0; i < terminalConfigs.length; i++) {
            var tc = terminalConfigs[i];
            if (tc.label && tc.labelPosition) {
                var style = {};
                style.position = "absolute";
                for (var prop in tc.labelPosition) {
                    var val = tc.labelPosition[prop];
                    if (typeof(val) == "number") {
                        val = val + "px";
                    }
                    style[prop] = val;
                }
                container.bodyEl.appendChild(WireIt.cn('div', null, style, tc.label));
            }
        }
    }
};

///////////////////////////////////////////////////////////////////////////

Wf.Editor = function(options) {

    Wf.Editor.superclass.constructor.call(this, options);

};

YAHOO.lang.extend(Wf.Editor, WireIt.WiringEditor);

Wf.Editor.prototype.renderButtons = function() {
    var toolbar = YAHOO.util.Dom.get('toolbar');

    var saveButton = new YAHOO.widget.Button({ label:"Save", id:"WiringEditor-saveButton", container: toolbar, className: "i18n"});
    saveButton.on("click", this.onSave, this, true);

    var helpButton = new YAHOO.widget.Button({ label:"Help", id:"WiringEditor-helpButton", container: toolbar, className: "i18n"});
    helpButton.on("click", this.onHelp, this, true);
};

Wf.Editor.prototype.checkAutoLoad = function() {
    this.loadPipe("default");
    return true;
};

Wf.Editor.prototype.getPipeByName = function(name) {
    if (name == "default") {
        if (this.pipes.length > 0)
            return this.pipes[0].working;
        else
            return null;
    } else {
        return Wf.Editor.superclass.getPipeByName(this, name);
    }
};

///////////////////////////////////////////////////////////////////////////

Wf.OptionFieldsHelper = function() {
};

Wf.OptionFieldsHelper.showOptions = function(container) {
    if (!container.optionsForm) {
        var optionsParentEl = YAHOO.util.Dom.get("optionsForm");
        var groupParams = { parentEl: optionsParentEl, fields: container.optFields, collapsible: false };
        container.optionsForm = new inputEx.Group(groupParams);
        container.optionsForm.setContainer(container);

        for(var i = 0 ; i < container.optionsForm.inputs.length ; i++) {
            var field = container.optionsForm.inputs[i];
            field.setContainer(container);
        }

        if (container.optionsValue) {
            container.optionsForm.setValue(container.optionsValue);
        }
    }
};

Wf.OptionFieldsHelper.hideOptions = function(container) {
    if (container.optionsForm) {
        container.optionsValue = container.optionsForm.getValue();
    }
    container.optionsForm = null;
    var optionsParentEl = YAHOO.util.Dom.get("optionsForm");
    optionsParentEl.innerHTML = "";
};

Wf.OptionFieldsHelper.getValue = function(container) {
    var optValue;
    if (container.optionsForm) {
        optValue = container.optionsForm.getValue();
    } else if (container.optionsValue) {
        optValue = container.optionsValue;
    }
    return optValue;
};

Wf.OptionFieldsHelper.setValue = function(container, val) {
    if (val) {
        if (container.optionsForm) {
            container.optionsForm.setValue(val);
        } else {
            container.optionsValue = val;
        }
    }
};

///////////////////////////////////////////////////////////////////////////

/**
 * Override ListField method to pass container into sub field constructor
 */
inputEx.ListField.prototype.renderSubField = function(value) {
    var lang = YAHOO.lang, Event = YAHOO.util.Event, Dom = YAHOO.util.Dom;

    // Div that wraps the deleteButton + the subField
    var newDiv = inputEx.cn('div'), delButton;

    // Delete button
    if(this.options.useButtons) {
        delButton = inputEx.cn('img', {src: inputEx.spacerUrl, className: 'inputEx-ListField-delButton'});
        Event.addListener( delButton, 'click', this.onDelete, this, true);
        newDiv.appendChild( delButton );
    }

    // Instantiate the new subField
    var opts = lang.merge({}, this.options.elementType);

    // Retro-compatibility with deprecated inputParams Object : TODO -> remove
    if(lang.isObject(opts.inputParams) && !lang.isUndefined(value)) {
        opts.inputParams.value = value;

        // New prefered way to set options of a field
    } else if (!lang.isUndefined(value)) {
        opts.value = value;
    }

    // KK
    opts.container = this.options.container;

    var el = inputEx(opts,this);

    var subFieldEl = el.getEl();
    Dom.setStyle(subFieldEl, 'margin-left', '4px');
    Dom.setStyle(subFieldEl, 'float', 'left');
    newDiv.appendChild( subFieldEl );

    // Subscribe the onChange event to resend it
    el.updatedEvt.subscribe(this.onChange, this, true);

    // Arrows to order:
    if(this.options.sortable) {
        var arrowUp = inputEx.cn('div', {className: 'inputEx-ListField-Arrow inputEx-ListField-ArrowUp'});
        Event.addListener(arrowUp, 'click', this.onArrowUp, this, true);
        var arrowDown = inputEx.cn('div', {className: 'inputEx-ListField-Arrow inputEx-ListField-ArrowDown'});
        Event.addListener(arrowDown, 'click', this.onArrowDown, this, true);
        newDiv.appendChild( arrowUp );
        newDiv.appendChild( arrowDown );
    }

    // Delete link
    if(!this.options.useButtons) {
        delButton = inputEx.cn('a', {className: 'inputEx-List-link'}, null, this.options.listRemoveLabel);
        Event.addListener( delButton, 'click', this.onDelete, this, true);
        newDiv.appendChild( delButton );
    }

    // Line breaker
    newDiv.appendChild( inputEx.cn('div', null, {clear: "both"}) );

    this.childContainer.appendChild(newDiv);

    return el;
};
