/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.MultiOutContainer = function(options, layer) {
    this.outputLabels = {};
    Wf.MultiOutContainer.superclass.constructor.call(this, options, layer);
    this.eventAddOutput = new YAHOO.util.CustomEvent("eventAddOutput");
    this.eventDelOutput = new YAHOO.util.CustomEvent("eventDelOutput");
    this.eventOutputChanged = new YAHOO.util.CustomEvent("eventOutputChanged");
};

YAHOO.lang.extend(Wf.MultiOutContainer, Wf.Container, {


    xtype: "Wf.MultiOutContainer",
    preventSelfWiring: false,


    render: function() {
        Wf.MultiOutContainer.superclass.render.call(this);

        var className = "Wf-MultiOutContainer-link";

        var buttonsDiv = WireIt.cn('div', {className: className}, {'float' : 'none'}, null);
        this.bodyEl.appendChild(buttonsDiv);

        var addBtn = WireIt.cn('div', {className: 'addButton'}, null, null);
        buttonsDiv.appendChild(addBtn);
        YAHOO.util.Event.addListener(addBtn, "click", this.addOutput, this, true);

        var delBtn = WireIt.cn('div', { className: 'deleteButton'}, null, null);
        buttonsDiv.appendChild(delBtn);
        YAHOO.util.Event.addListener(delBtn, "click", this.deleteOutput, this, true);
        /*
        var changeDiv = WireIt.cn('div',{className : "Wf-MultiOutContainer-arrow"},null,null);
        this.bodyEl.appendChild(changeDiv);
        */
        var changeArrow  = WireIt.cn('div',{className: "Wf-MultiOutContainer-change"},null,null);

		this.ddHandle.appendChild(changeArrow);

        YAHOO.util.Event.addListener(changeArrow,"click",this.changeDirection,this,true);
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
        var pattern = /\S+/;
        if (!pattern.test(name)) {
            this.closeNewOutEditor();
            return;
        }
        var bottomPos=-15;
        var topPos=undefined;
        if (this.getOutputs()[0]){
            bottomPos=this.getOutputs()[0].offsetPosition.bottom;
            topPos=this.getOutputs()[0].offsetPosition.top;
        }

        if (this.getTerminal(name)){
         this.closeNewOutEditor();
         return;
        }

        this.addTerminal({
            "name": name, "direction": [0,1],
            "offsetPosition": {"left": 150, "bottom": bottomPos, "top" : topPos},
            "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
        });

        this.closeNewOutEditor();
        this.renderOutputs();

        this.eventAddOutput.fire(name);
        this.layer.eventChanged.fire();
    },


    renderOutputs: function() {
        var outputs = this.getOutputs();
        var offset;
        var margin=Math.round(20/Math.sqrt((outputs.length-1.9)));
        if(outputs.length==1)
            offset = Math.round((this.width)/2);
        else
            offset = Math.round((this.width-margin*2) / (outputs.length-1));

        for (var i = 0; i < outputs.length; i++) {
            var output = outputs[i];

            var style;
            if (outputs.length == 1) {
                output.offsetPosition.left = offset;
            }
            else
                output.offsetPosition.left = offset * (i)+margin;
            if (this.direction=="down"){
                output.setPosition({left: output.offsetPosition.left-14, bottom:-15});
                style = {position: "absolute", left: output.offsetPosition.left-Math.round(offset/2)+4+"px", bottom: "-25px",
                    top: "auto", width:offset-8+"px" };
                output.direction= [0,1];
            }
            else if(this.direction=="up"){
                output.setPosition({left: output.offsetPosition.left-14, top:-15});
                style = {position: "absolute", left: output.offsetPosition.left-Math.round(offset/2)+4+"px",
                    top:"-25px",bottom: "auto", width:offset-8+"px"};
                output.direction= [0,-1];
            }

            var labelEditor = this.outputLabels[output.name];
            if (!labelEditor) {
                    labelEditor = this.createLabelEditor(output.name);
                //labelEditor.setValue(output.name);
                WireIt.sn(labelEditor.getEl(), null, style);
                labelEditor.updatedEvt.subscribe(this.onLabelChanged, [labelEditor,output,this.outputLabels, this.layer], true);
                this.outputLabels[output.name] = labelEditor;
            } else {
                WireIt.sn(labelEditor.getEl(), {className:"terminalLabel"}, style);
            }
        }
        this.redrawAllWires();
    },

    createLabelEditor: function(name){
        return new inputEx.StringField({
                                   className: "terminalLabel", parentEl: this.bodyEl , value:name});
    },

    onLabelChanged: function(type, args, scope) {
        var pattern = /\S+/;
        var labelEditor = scope[0];
        var output = scope[1];
        var outputLabels = scope[2];
        var layer = scope[3];
        var prevVal = output.name;

        if (!pattern.test(args[0])||outputLabels[args[0]]) {
            labelEditor.setValue(output.name);
            return;
        }

        delete outputLabels[output.name];
        output.name = args[0];
        outputLabels[output.name] = labelEditor;
        output.container.eventOutputChanged.fire({prevValue: prevVal, value: output.name});
        layer.eventChanged.fire();
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
            lab.destroy();
            //this.bodyEl.removeChild(lab);
            delete this.outputLabels[name];
        }

        this.closeDelOutEditor();
        this.renderOutputs();

        this.eventDelOutput.fire(name);
        this.layer.eventChanged.fire();
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
