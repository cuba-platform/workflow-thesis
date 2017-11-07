/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

Wf.DesignContainer = function(options, layer) {
    Wf.DesignContainer.superclass.constructor.call(this, options, layer);
    this.removeAllOutputs();
};

YAHOO.lang.extend(Wf.DesignContainer, Wf.MultiOutContainer, {

      xtype: "Wf.DesignContainer",
      preventSelfWiring: false,
      previousValue:null,

      render: function() {
              Wf.MultiOutContainer.superclass.render.call(this);
              var changeArrow  = WireIt.cn('div',{className: "Wf-MultiOutContainer-change"},null,null);
              this.ddHandle.appendChild(changeArrow);
              YAHOO.util.Event.addListener(changeArrow,"click",this.changeDirection,this,true);

      },

      initTerminals: function(terminalConfigs) {
              Wf.DesignContainer.superclass.initTerminals.call(this, terminalConfigs);

      },

      onUpdateForm : function(type, args) {
            this.optionsValue = args[0];
            Wf.DesignContainer.superclass.onUpdateForm.call(this, type, args);
         },

      removeAllOutputs: function() {
            if (this.getOutputs()!=null && this.getOutputs().length!=null) {
                while (this.getOutputs().length>0){
                    var output = this.getOutputs()[0];
                    this.removeOutput(null,[output.name]);
                }
            }
      },

      createLabelEditor: function(name){
            var field = new inputEx.UneditableField({
                                         className: "terminalLabel", parentEl: this.bodyEl , value:name});
            WireIt.sn(field.fieldContainer, null, {float : "none" , "text-align" : "center"});
            return field;
      }
});