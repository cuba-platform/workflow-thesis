/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.ImageContainer = function(options, layer) {
    Wf.ImageContainer.superclass.constructor.call(this, options, layer);

    this.eventFocus.subscribe(this.onContainerFocus, this, true);
    this.eventBlur.subscribe(this.onContainerBlur, this, true);
};

YAHOO.lang.extend(Wf.ImageContainer, WireIt.ImageContainer, {

    xtype: "Wf.ImageContainer",
    direction: "down",

    selectedImage: null,

    render: function(){
        Wf.ImageContainer.superclass.render.call(this);

        this.turnButton = WireIt.cn('div', {className: "WireIt-ImageContainer-turnbutton"} );
			if (this.ddHandle) {
				this.ddHandle.appendChild(this.turnButton);
			}
			else {
				this.el.appendChild(this.turnButton);
			}
         YAHOO.util.Event.addListener(this.turnButton, "click", this.onTurnButton, this, true);
    },

    initTerminals: function(terminalConfigs) {
        Wf.ImageContainer.superclass.initTerminals.call(this, terminalConfigs);
        Wf.initTerminalLabels(this, terminalConfigs);
        if (this.direction!="down"){this.turnTerminals()}
        
    },

    turnTerminals: function(){
        for (var i = 0; i < this.terminals.length; i++) {
            var terminal = this.terminals[i];
            var offsetPosition = terminal.offsetPosition;
            var newOffsetPosition = {};
            newOffsetPosition.left = terminal.offsetPosition.left ? terminal.offsetPosition.left : "";
            newOffsetPosition.right = terminal.offsetPosition.right ? terminal.offsetPosition.right : "";
            if (offsetPosition.top) {
                newOffsetPosition.bottom = offsetPosition.top;
            }
            else if (offsetPosition.bottom) {
                newOffsetPosition.top = offsetPosition.bottom;
            }
            var newDirection = [0,0];
            newDirection[0] = terminal.direction[0];

            if (terminal.direction[1] == -1) {
                newDirection[1] = 1;
            }
            else if (terminal.direction[1] == 1) {
                newDirection[1] = -1;
            }
            terminal.direction = newDirection;
            terminal.offsetPosition = newOffsetPosition;
            terminal.setPosition(newOffsetPosition);

        }
        this.redrawAllWires();
    },

    onContainerFocus: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.showOptions(container);
        YAHOO.util.Dom.setStyle(this.bodyEl, "background-image", "url("+this.selectedImage+")");
    },

    onContainerBlur: function(eventName, containers) {
        var container = containers[0];
        Wf.OptionFieldsHelper.hideOptions(container);
        YAHOO.util.Dom.setStyle(this.bodyEl, "background-image", "url("+this.image+")");
    },

    onCloseButton: function(e, args) {
        Wf.ImageContainer.superclass.onCloseButton.call(this, e, args);
        Wf.OptionFieldsHelper.hideOptions(this);
    },

    onTurnButton: function(e, args){
        if(this.direction=="down"){this.direction="up";}
        else if(this.direction=="up"){this.direction="down";}
        this.turnTerminals();
        this.layer.eventChanged.fire();
    },

    getConfig: function() {
      return {
			position: this.getXY(),
			xtype: this.xtype,
            direction: this.direction
      };
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
        var value = Wf.ImageContainer.superclass.getValue.call(this);
        value.options = Wf.OptionFieldsHelper.getValue(this);
        value.variables = Wf.OptionFieldsHelper.getVariables(this);
        value.name=value.options['name'];
        value.outputs = [];
        var outputs = this.getOutputs();
        for (var i = 0; i < outputs.length; i++) {
            var out = outputs[i];
            value.outputs.push({name: out.name});
        }
        return value;
    },

    setValue: function(val) {
        Wf.ImageContainer.superclass.setValue.call(this, val);
        Wf.OptionFieldsHelper.setValue(this, val.options);
        Wf.OptionFieldsHelper.setVariables(this, val.variables);
    }
});
