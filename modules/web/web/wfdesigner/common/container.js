/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.Container = function(options, layer) {
    var initialized=false;
    var optionsInitialized=false;

    Wf.Container.superclass.constructor.call(this, options, layer);

    this.eventFocus.subscribe(this.onContainerFocus, this, true);
    this.eventBlur.subscribe(this.onContainerBlur, this, true);
    this.form.updatedEvt.subscribe(function(){
         if(!initialized){
             initialized=true;
         }
         else{
            layer.eventChanged.fire();
         }
    });
    this.form.updatedEvt.subscribe(this.onUpdateForm,this,true);

    var nameField = this.form.getFieldByName('name');

    this.focused=false;

    nameField.updatedEvt.subscribe(
            Wf.OptionFieldsHelper.onNameChanged,
            nameField, true);


};

YAHOO.lang.extend(Wf.Container, WireIt.FormContainer, {

    xtype: "Wf.Container",
    direction :"down",
    variables: null,


    render: function() {
        Wf.Container.superclass.render.call(this);

        var changeArrow  = WireIt.cn('div',{className: "Wf-Container-change"},null,null);

		this.ddHandle.appendChild(changeArrow);
        YAHOO.util.Event.addListener(changeArrow,"click",this.changeDirection,this,true);
     },

    initTerminals: function(terminalConfigs) {
        Wf.Container.superclass.initTerminals.call(this, terminalConfigs);
        this.renderInput();
        this.renderOutputs();
        Wf.initTerminalLabels(this, terminalConfigs);
    },

       /**
    * Return the config of this container.
    * @method getConfig
    */
   getConfig: function() {
      return {
			position: this.getXY(),
			xtype: this.xtype,
			direction: this.direction
		};
   },

   onUpdateForm : function(type, args) {

      if(!this.optionsValue){
          this.optionsValue = this.optionsForm.getValue();
      }
      for(key in args[0]){
          this.optionsValue[key] = args[0][key];
      }

      this.optionsForm.setValue(this.optionsValue,false);
   },

    onContainerFocus: function(eventName, containers) {

        var container = containers[0];
        if (container.focused == true)
            return;
        container.focused = true;
        var nameField = container.form.getFieldByName('name');
        nameField.prevValue = nameField.getValue();
        container.optionsInitialized=false;
        Wf.OptionFieldsHelper.showOptions(container);
    },

    onContainerBlur: function(eventName, containers) {
        var container = containers[0];
        if (container.focused == false)
            return;
        container.focused = false;
        Wf.OptionFieldsHelper.hideOptions(container);
    },

    onCloseButton: function(e, args) {
        Wf.Container.superclass.onCloseButton.call(this, e, args);
        Wf.OptionFieldsHelper.hideOptions(this);
    },

    getValue: function() {
        var value = Wf.Container.superclass.getValue.call(this);
        value.options = Wf.OptionFieldsHelper.getValue(this);
        value.variables = Wf.OptionFieldsHelper.getVariables(this);
        value.outputs = [];

        var outputs = this.getOutputs();
        for (var i = 0; i < outputs.length; i++) {
            var out = outputs[i];
            value.outputs.push({name: out.name});
        }
        return value;
    },

    setValue: function(val) {

        if (val.options) {
            for (key in val.options) {
                if (val[key] == null) {
                    val[key] = val.options[key];
                }
            }
        }


        Wf.Container.superclass.setValue.call(this, val);
        Wf.OptionFieldsHelper.setValue(this, val.options);
        Wf.OptionFieldsHelper.setVariables(this, val.variables);
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

    getInput : function() {
      var input;
        for(var i=0;i<this.terminals.length; i++){
            var terminal = this.terminals[i];
            if(terminal.ddConfig.type == "in"){return terminal;}
        }
      return null;
    },

    renderInput: function() {
      var input = this.getInput();
        if (this.direction=="down"){
                input.setPosition({left: input.offsetPosition.left,top:-15});
            input.direction= [0,-1];
        }
        else if(this.direction=="up"){
             input.setPosition({left: input.offsetPosition.left,bottom:-15});
            input.direction= [0,1];
        }
    },

    renderOutputs: function() {
        var outputs = this.getOutputs();
        var offset = Math.round((this.width - 30) / (outputs.length + 1));

        for (var i = 0; i < outputs.length; i++) {
            var output = outputs[i];


            if (this.direction=="down"){
                output.setPosition({left: output.offsetPosition.left, bottom:-15});

                output.direction= [0,1];
            }
            else if(this.direction=="up"){
                output.setPosition({left: output.offsetPosition.left, top:-15});

                output.direction= [0,-1];
            }

        }
        var style;
        if (this.direction=="down"){
            style = { bottom: "-15px", top: "auto"};
        }
        else if(this.direction=="up"){
            style = { top:"-15px",bottom: "auto"};
        }
         var labels =  YAHOO.util.Dom.getElementsByClassName("terminalLabel","div",this.bodyEl);
            for(var j=0;j<labels.length;j++){
                var currLabel = labels[j];
                WireIt.sn(currLabel,null,style);
            }
        this.redrawAllWires();
    },

    changeDirection : function() {
        if (this.direction == "down")
            this.direction = "up";

        else if (this.direction == "up")
            this.direction = "down";

        this.renderInput();
        this.renderOutputs();
        this.layer.eventChanged.fire();
    }

});
