/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 28.01.11 12:10
 *
 * $Id$
 */
Wf.MultiOutContainer = function(options, layer) {
    Wf.MultiOutContainer.superclass.constructor.call(this, options, layer);

    this.outputLabels = {};

    this.eventAddOutput = new YAHOO.util.CustomEvent("eventAddOutput");
    this.eventDelOutput = new YAHOO.util.CustomEvent("eventDelOutput");
};

YAHOO.lang.extend(Wf.MultiOutContainer, Wf.Container, {

    xtype: "Wf.MultiOutContainer",

    render: function() {
        Wf.MultiOutContainer.superclass.render.call(this);

        var className = "Wf-MultiOutContainer-link";

        var buttonsDiv = WireIt.cn('div', {className: className}, null, null);
        this.bodyEl.appendChild(buttonsDiv);

        var addBtn = WireIt.cn('a', {href: "#", className: className}, null, i18n.get('MultiOutContainer.add'));
        buttonsDiv.appendChild(addBtn);
        YAHOO.util.Event.addListener(addBtn, "click", this.addOutput, this, true);

        var delBtn = WireIt.cn('a', {href: "#", className: className}, null, i18n.get('MultiOutContainer.delete'));
        buttonsDiv.appendChild(delBtn);
        YAHOO.util.Event.addListener(delBtn, "click", this.deleteOutput, this, true);
    },

    addOutput: function(e) {
        YAHOO.util.Event.stopEvent(e);
        if (!this.newOutEditor) {
            this.newOutEditor = new inputEx.InPlaceEdit({
                parentEl: this.bodyEl,
                editorField:{type:'string'}, animColors:{from:"#FFFF99", to:"#DDDDFF"}
            });
            this.newOutEditor.updatedEvt.subscribe(this.createOutput, this, true);
            this.newOutEditor.cancelLink.clickEvent.subscribe(this.closeNewOutEditor, this, true);
            this.newOutEditor.openEditor();
        }
    },

    closeNewOutEditor: function() {
        if (this.newOutEditor) {
            this.bodyEl.removeChild(this.newOutEditor.getEl());
            this.newOutEditor = undefined;
        }
    },

    createOutput: function(e, params) {
        var i;
        var name = params[0];

        if (this.getTerminal(name))
            return;

        this.addTerminal({
            "name": name, "direction": [0,1],
            "offsetPosition": {"left": 150, "bottom": -15},
            "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
        });

        this.closeNewOutEditor();
        this.renderOutputs();

        this.eventAddOutput.fire(name);
    },

    renderOutputs: function() {
        var outputs = this.getOutputs();
        var offset = Math.round((this.width - 30) / (outputs.length + 1));
        for (var i = 0; i < outputs.length; i++) {
            var output = outputs[i];
            output.offsetPosition.left = offset * (i+1);
            output.setPosition({left: output.offsetPosition.left, bottom: -15});

            var style = {position: "absolute", left: output.offsetPosition.left+20+"px", bottom: "-15px"};
            var lab = this.outputLabels[output.name];
            if (!lab) {
                lab = WireIt.cn('div', null, style, output.name);
                this.bodyEl.appendChild(lab);
                this.outputLabels[output.name] = lab;
            } else {
                WireIt.sn(lab, null, style);
            }
        }
        this.redrawAllWires();
    },

    deleteOutput: function(e) {
        YAHOO.util.Event.stopEvent(e);
        if (!this.delOutEditor) {
            var choices = [];
            var outputs = this.getOutputs();
            for (var i = 0; i < outputs.length; i++) {
                choices.push(outputs[i].name);
            }

            this.delOutEditor = new inputEx.InPlaceEdit({
                parentEl: this.bodyEl,
                editorField:{type:'select', choices: choices}, animColors:{from:"#FFFF99", to:"#DDDDFF"}
            });
            this.delOutEditor.updatedEvt.subscribe(this.removeOutput, this, true);
            this.delOutEditor.cancelLink.clickEvent.subscribe(this.closeDelOutEditor, this, true);
            this.delOutEditor.openEditor();
        }
    },

    closeDelOutEditor: function() {
        if (this.delOutEditor) {
            this.bodyEl.removeChild(this.delOutEditor.getEl());
            this.delOutEditor = undefined;
        }
    },

    removeOutput: function(e, params) {
        var i;
        var name = params[0];

        if (!this.getTerminal(name))
            return;

        var idx;
        for(i = 0 ; i < this.terminals.length ; i++) {
            var terminal = this.terminals[i];
            if (terminal.ddConfig.type == "out" && terminal.name == name) {
                terminal.remove();
                idx = i;
                break;
            }
        }
        this.terminals.splice(idx, 1);

        var lab = this.outputLabels[name];
        if (lab) {
            this.bodyEl.removeChild(lab);
            delete this.outputLabels[name];
        }

        this.closeDelOutEditor();
        this.renderOutputs();

        this.eventDelOutput.fire(name);
    },

    getOutputs: function() {
        var outputs = [];
        for (var i = 0; i < this.terminals.length; i++) {
            var terminal = this.terminals[i];
            if (terminal.ddConfig.type == "out")
                outputs.push(terminal);
        }
        return outputs;
    },

    getValue: function() {
        var value = Wf.MultiOutContainer.superclass.getValue.call(this);
        value.outputs = [];

        var outputs = this.getOutputs();
        for (var i = 0; i < outputs.length; i++) {
            var out = outputs[i];
            value.outputs.push({name: out.name, position: out.offsetPosition});
        }

        return value;
    },

    setValue: function(val) {
        if (val.outputs) {
            for (var i = 0; i < val.outputs.length; i++) {
                var v = val.outputs[i];
                if (!this.getTerminal(v.name)) {
                    this.addTerminal({
                        name: v.name, offsetPosition: v.position,
                        direction: [0,1], ddConfig: {type: "out", allowedTypes: ["in"]}, alwaysSrc: true
                    });
                }
            }
        }
        this.renderOutputs();
        Wf.MultiOutContainer.superclass.setValue.call(this, val);
    }
});
