(function() {

   var lang = YAHOO.lang;

/**
 * Copy of the original inputEx.Field class that we're gonna override to extend it.
 * @class BaseField
 * @namespace inputEx
 */
inputEx.BaseField = inputEx.Field;

/**
 * Class to make inputEx Fields "wirable".Re-create inputEx.Field adding the wirable properties
 * @class Field
 * @namespace inputEx
 * @extends inputEx.BaseField
 */
inputEx.Field = function(options) {
   inputEx.Field.superclass.constructor.call(this,options);
};

lang.extend(inputEx.Field, inputEx.BaseField, {

    addVariableBtn: null,
    delVariableBtn: null,
    variableAliasInput: null,
    hasVariableBlock: null,

   /**
    * Adds a wirable option to every field
    * @method setOptions
    */
   setOptions: function(options) {
      inputEx.Field.superclass.setOptions.call(this, options);
      
      this.options.wirable = lang.isUndefined(options.wirable) ? false : options.wirable;
      this.options.container = options.container;
      if (options.allowVariable!=null) {
        this.options.allowVariable = options.allowVariable;
      }
      //options.container = null;
   },
   
   /**
    * Adds a terminal to each field
    * @method render
    */
   render: function() {
      inputEx.Field.superclass.render.call(this);
      
      if(this.options.wirable) {
         this.renderTerminal();
      }
      this.initVariableStyle();
   },

    setVariableStyle: function(){
        if (this.allowVariable != false){
            this.allowVariable = this.options.allowVariable;
        }
        if (this.allowVariable != false){
             if (this.options.container.variables == null) this.options.container.variables = new Object();
             var variableAlias = this.options.container.variables[this.options.name];
             var parent = this.el;
             if (this.el.type == "checkbox"){
                 parent = this.el.parentElement
             }
             if (variableAlias != null && variableAlias!=""){
                 WireIt.sn(parent, null, {width : "120px"});
                 WireIt.sn(this.hasVariableBlock, null, {display : "block"});
             }
             else {
                WireIt.sn(this.hasVariableBlock, null, {display : "none"});
                WireIt.sn(parent, null, {width : "140px"});
             }
        }
   },


    initVariableStyle: function(){
        if (this.el==null) return;
        var parent = this.el;
        if (this.el.type == "checkbox"){
             parent = this.el.parentElement
        }
        var className = "Wf-Container-hasVariable";
        this.hasVariableBlock = WireIt.cn('div', {className: className}, {float : "right", display : "none"}, null);
        YAHOO.util.Dom.insertAfter(this.hasVariableBlock, parent);
   },

    initVariableButtons: function(){
        if (this.el==null) return;
        if (this.allowVariable!=false){
            this.allowVariable = this.options.allowVariable;
        }
        if (this.allowVariable!=false)
        {
             var className = "Wf-Container-with-variable";
             var parent=this.el;
             var buttonsDiv = WireIt.cn('div', {className: className}, {float : "right"}, null);
             if (this.el.type == "checkbox"){
                 parent = this.el.parentElement.parentElement
             }
             YAHOO.util.Dom.insertBefore(buttonsDiv,parent);
             //parent.appendChild(buttonsDiv);

             this.addVariableBtn = WireIt.cn('div', {className: 'variableButton'}, null, null);
             buttonsDiv.appendChild(this.addVariableBtn);
             YAHOO.util.Event.addListener(this.addVariableBtn, "click", this.addVariable, this, true);

             this.delVariableBtn = WireIt.cn('div', { className: 'deleteButton'}, null, null);
             buttonsDiv.appendChild(this.delVariableBtn);
             YAHOO.util.Event.addListener(this.delVariableBtn, "click", this.deleteVariable, this, true);

             this.variableAliasInput = inputEx({type: 'string', name: 'variable', label: i18nDict.Variable});
             WireIt.sn(this.variableAliasInput.divEl,{className:"Wf-Container-with-variable-input"},null);
             YAHOO.util.Dom.insertAfter(this.variableAliasInput.divEl,parent);
             if (this.options.container.variables==null) this.options.container.variables = new Object();
             var variableAlias = this.options.container.variables[this.options.name];
             if (variableAlias!=null && variableAlias!=""){
                 this.variableAliasInput.setValue(variableAlias);
                 this.addVariable();
             }
             else {
                 this.deleteVariable();
             }
        }
    },

    addVariable: function(){
            WireIt.sn(this.delVariableBtn,null,{display : "block"});
            WireIt.sn(this.addVariableBtn,null,{display : "none"});
            WireIt.sn(this.variableAliasInput.divEl,null,{display : "block"});
    },

    deleteVariable: function() {
            WireIt.sn(this.delVariableBtn,null,{display : "none"});
            WireIt.sn(this.addVariableBtn,null,{display : "block"});
            WireIt.sn(this.variableAliasInput.divEl,null,{display : "none"});
            this.variableAliasInput.setValue(null);
    },
   
   /**
    * Render the associated input terminal
    * @method renderTerminal
    */
   renderTerminal: function() {

      var wrapper = inputEx.cn('div', {className: 'WireIt-InputExTerminal'});
      this.divEl.insertBefore(wrapper, this.fieldContainer);

      this.terminal = new WireIt.Terminal(wrapper, {
         name: this.options.name, 
         direction: [-1,0],
         fakeDirection: [0, 1],
         ddConfig: {
            type: "input",
            allowedTypes: ["output"]
         },
      nMaxWires: 1 }, this.options.container);

      // Reference to the container
      if(this.options.container) {
         this.options.container.terminals.push(this.terminal);
      }

      // Register the events
      this.terminal.eventAddWire.subscribe(this.onAddWire, this, true);
      this.terminal.eventRemoveWire.subscribe(this.onRemoveWire, this, true);
    },

	/**
	 * Set the container for this field
	 */
	setContainer: function(container) {
		this.options.container = container;
		if(this.terminal) {
			this.terminal.container = container;
			if( WireIt.indexOf(this.terminal, container.terminals) == -1 ) {
				container.terminals.push(this.terminal);
			}
		}		
	},

	/**
	 * also change the terminal name when changing the field name
	 */
	setFieldName: function(name) {
		if(this.terminal) {
			this.terminal.name = name;
			this.terminal.el.title = name;
		}
	},

    /**
     * Remove the input wired state on the 
     * @method onAddWire
     */
    onAddWire: function(e, params) {
       this.options.container.onAddWire(e,params);

       this.disable();
       this.el.value = "[wired]";
    },

    /**
     * Remove the input wired state on the 
     * @method onRemoveWire
     */
    onRemoveWire: function(e, params) { 
       this.options.container.onRemoveWire(e,params);

       this.enable();
       this.el.value = "";
    }

});

inputEx.Field.groupOptions = inputEx.BaseField.groupOptions.concat([
	{ type: 'boolean', label: 'Wirable', name: 'wirable', value: false}
]);

})();