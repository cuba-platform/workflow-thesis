/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

WireIt.Terminal.prototype.wireConfig = {
    xtype: "Wf.BezierArrowWire",
    width: 2,
    borderwidth: 0,
    color: 'rgb(55,55,56)',
    bordercolor: '#373738'

};


WireIt.Terminal.prototype.editingWireConfig = WireIt.Terminal.prototype.wireConfig;

WireIt.Container.prototype.resizable=false;

Wf = {

    createProcIdParam: function() {
            var s = window.location.search;
            if (s && s.charAt(0) == "?") {
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

    loadJson:function (url, callback, scope) {
        YAHOO.util.Connect.asyncRequest(
            'GET',
            url,
            {
                success:function (o) {
                    var r = YAHOO.lang.JSON.parse(o.responseText);
                    callback.call(scope, r);
                },
                failure:function (o) {
                    var error = o.status + " " + o.statusText;
                    console.log(error);
                }
            },
            null
        );
    },


    loadScripts: function(callback, scope) {
        this.loadJson('action/loadScripts.json' + Wf.createProcIdParam(), callback, scope);
    },

    loadJbpmProcs:function (callback, scope) {
        this.loadJson('action/loadJbpmProcs.json', callback, scope);
    },

    loadSubDesigns: function(callback, scope) {
        this.loadJson('action/loadSubDesigns.json', callback, scope);
    },

    loadOperationTypes: function(callback, scope) {
        this.loadJson('action/loadOperationTypes.json', callback, scope);
    },

    loadCardInheritors: function(callback, scope) {
        this.loadJson('action/loadCardInheritors.json', callback, scope);
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
                var labelDiv = WireIt.cn( 'div', { 'class':'terminalLabel', 'name':tc.label } , style, tc.label);
                container.bodyEl.appendChild(labelDiv);
            }
        }
    },

    parseHtmlEntities: function(str) {
        var decoder = document.createElement('textarea');
        decoder.innerHTML = str;
        return decoder.value;
    }
};

///////////////////////////////////////////////////////////////////////////

Wf.Editor = function(options) {

    Wf.Editor.superclass.constructor.call(this, options);

};

YAHOO.lang.extend(Wf.Editor, WireIt.WiringEditor,{

    allowVariable: true,

    render: function() {
        WireIt.WiringEditor.superclass.render.call(this);
	    this.layer = new Wf.Layer(this.options.layerOptions);
		this.layer.eventChanged.subscribe(this.onLayerChanged, this, true);
        // Left Accordion
		this.renderModulesAccordion();
        // Render module list
	    this.buildModulesList();
        //render errorMEssagesPanel
        Wf.Editor.messagePanel = new YAHOO.widget.Panel("messagePanel", {width:"150px", visible:false, draggable:false, close:false, effect:{effect:YAHOO.widget.ContainerEffect.FADE,duration:0.35} } );
  	},

    renderButtons : function() {
        var toolbar = YAHOO.util.Dom.get('toolbar');

        var saveButton = new YAHOO.widget.Button({ label:i18nDict.Save + '&nbsp;', id:"WiringEditor-saveButton", container: toolbar, className: "i18n"});
        saveButton.on("click", this.onSave, this, true);
        saveButton.isSaved = true;
        this.saveButton = saveButton;

        var helpButton = new YAHOO.widget.Button({ label:i18nDict.Help, id:"WiringEditor-helpButton", container: toolbar, className: "i18n"});
        helpButton.on("click", this.onHelp, this, true);

        var layoutButton = new YAHOO.widget.Button({label:i18nDict.align,container:toolbar});
        layoutButton.on("click", this.onLayoutBtn, this, true);
    },

    onLayoutBtn: function() {
        var containers = this.layer.containers;
        for (var i = 0; i < containers.length; i++) {
            var container = containers[i];
            var containerWidth = container.bodyEl.parentNode.clientWidth;
            var newPos = this.calculateGridPosition(container.getXY(), containerWidth);
            container.dd.getDragEl().style.top = newPos[1] + 'px';
            container.dd.getDragEl().style.left = newPos[0] + 'px';
            container.redrawAllWires();
        }
        this.layer.eventChanged.fire();
    },

    calculateGridPosition: function(pos, containerWidth) {
        var width = 130;
        var height = 80;
        var xPos = Math.round((pos[0] + containerWidth / 2) / width) * width + 20;
        var yPos = Math.round(pos[1] / height) * height + 20;
        return new Array(xPos - containerWidth / 2, yPos);
    },

    renderSavedStatus: function() {
		
	},

    markSaved: function() {
        this.saveButton.set("label" , i18nDict.Save + '&nbsp;');
        this.saveButton.isSaved=true;
    },

	markUnsaved: function() {
        this.saveButton.set("label" , i18nDict.Save + '*');
        this.saveButton.isSaved=false;
    },

    isSaved: function() {
		return this.saveButton.isSaved;
	},


    onSave : function(){
      var value = this.getValue();

    	if(!value.name.match(/\S+/)) {
       	    this.alert(i18nDict.ChooseName);
       	    return;
    	}

		this.tempSavedWiring = {name: value.name, working: value.working, language: this.options.languageName };
        this.adapter.saveWiring(this.tempSavedWiring, {
       	    success: this.saveModuleSuccess(this.checkModules(value)),
       	    failure: this.saveModuleFailure,
       	    scope: this
    	});
    },

    checkModules : function(value){
        var modules = value.working.modules;
        var wires = value.working.wires;
        var moduleDefinitions = this.modules;
        for(var i = 0; i<modules.length;i++){
            var module = modules[i];
            var moduleDefinition = this.findModuleDef(module,moduleDefinitions);
            var outputs = new Array();
            var inputs = new Array();
            var terminals = moduleDefinition.container.terminals;
            if (module.value.outputs) {
                outputs = module.value.outputs
            }
            for (j = 0; j < terminals.length; j++) {
                var terminal = terminals[j];

                if (!module.value.outputs && terminal.ddConfig.type == 'out') {
                    outputs.push(terminal);
                } else if (terminal.ddConfig.type == 'in') {
                    inputs.push(terminal)
                }
            }
            if(!this.checkWires(wires,i,outputs,inputs)){
                return i18nDict.moduleWithoutTransition;
            }

            if (!this.checkRequiredFields(module)) {
                return i18nDict.moduleWithEmptyRequiredFields;
            }

        }

        return null;
    },

    checkWires: function(wires ,moduleId, outputs, inputs){
       //ищем вход в модуль и все выходы. их число должно совпасть с размером outputs.
        var outputWires = new Array();
        var inputWires = new Array();
        // ищем вход в модуль
        for (var i = 0; i < wires.length; i++) {
            var wire = wires[i];
            if(wire.src.moduleId == moduleId){
                outputWires.push(wire)
            }
            if(wire.tgt.moduleId == moduleId){
                inputWires.push(wire);
            }
        }

        return inputWires.length >= inputs.length && outputWires.length == outputs.length;
    },

    //check that fields marked as required (lang.js) are filled in a module
    checkRequiredFields: function(module) {
        var moduleDescription = this.findModuleDef(module, this.modules);
        var fields = moduleDescription.container.fields;
        if (fields != undefined) {
            for (var i = 0; i < fields.length; i++) {
                if (fields[i].required == true) {
                    var fieldValue = module.value[fields[i].name];
                    if (fieldValue === undefined || fieldValue === null || fieldValue === "") {
                        return false;
                    }
                }
            }
        }

        var optFields = moduleDescription.container.optFields;
        if (optFields != undefined) {
            for (var i = 0; i < optFields.length; i++) {
                if (optFields[i].required == true) {
                    var optFieldValue = module.value.options[optFields[i].name];
                    if (optFieldValue === undefined || optFieldValue === null || optFieldValue === "") {
                        return false;
                    }
                }
            }

        }

        return true;
    },

    findModuleDef : function(module, moduleDefinitions) {
        var moduleName = module.name;
        for (var i = 0; i < moduleDefinitions.length; i++) {
            if (moduleDefinitions[i].name == moduleName) {
                return moduleDefinitions[i];
            }
        }
        return null;
    },

    saveModuleSuccess: function(o) {

        this.markSaved();
        if (o==null) {
            this.alert(i18nDict.Saved);
        } else {
            this.alert(o)
        }
    },
    renderAlertPanel: function() {
    /**
     * @property alertPanel
     * @type {YAHOO.widget.Panel}
     */
		this.alertPanel = new YAHOO.widget.Panel('WiringEditor-alertPanel', {
         fixedcenter: true,
         draggable: true,
         width: '300px',
         visible: false,
         modal: true
      });
      this.alertPanel.setHeader(i18nDict.AlertMessage);
      this.alertPanel.setBody("<div id='alertPanelBody'></div><button id='alertPanelButton'>OK</button>");
      this.alertPanel.render(document.body);
		YAHOO.util.Event.addListener('alertPanelButton','click', function() {
			this.alertPanel.hide();
		}, this, true);
	},

    criticalAlert: function(txt) {
		if(!this.criticalAlertPanel){ this.renderCriticalAlertPanel(txt); }
		this.criticalAlertPanel.show();
	},

	renderCriticalAlertPanel: function(txt){
	    	this.criticalAlertPanel = new YAHOO.widget.Panel('WiringEditor-alertPanel', {
         fixedcenter: true,
         draggable: true,
         width: '300px',
         visible: false,
         modal: true,
         close: false
      });
      this.criticalAlertPanel.setHeader(i18nDict.AlertMessage);
      this.criticalAlertPanel.setBody("<div id='alertPanelBody'></div>");
      this.criticalAlertPanel.render(document.body);
      YAHOO.util.Dom.get('alertPanelBody').innerHTML = txt;
	},

      /**
	  * Start the loading of the pipes using the adapter
	  * @method load
	  */
    load: function() {
        if ((navigator.appName.indexOf("Explorer")) != -1) {
            this.criticalAlert(i18nDict.TurnTheChromeFrame);
            CFInstall.check({mode: "inline", node: "alertPanelBody", preventInstallDetection:"true"});
            return;
        }

	    this.adapter.listWirings({language: this.options.languageName},{
			success: function(result) {
				this.onLoadSuccess(result);
			},
			failure: function(errorStr) {
				this.alert("Unable to load the wirings: "+errorStr);
			},
			scope: this
		});

    }

});


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
        container.optionsForm.updatedEvt.subscribe(
                function() {
                    if(!container.optionsInitialized){
                        container.optionsInitialized=true;
                    }
                    else{
                        container.layer.eventChanged.fire();
                    }
                });

        for(var i = 0 ; i < container.optionsForm.inputs.length ; i++) {
            var field = container.optionsForm.inputs[i];
            field.setContainer(container);
            if (Wf.editor.allowVariable) {
                if ("name" != container.optFields[i].name){
                    field.initVariableButtons();
                }
            }
        }

        if (container.optionsValue) {
            container.optionsForm.setValue(container.optionsValue);
        }

        var nameField = container.optionsForm.getFieldByName('name');
        nameField.prevValue = nameField.getValue();
        nameField.updatedEvt.subscribe(
                Wf.OptionFieldsHelper.onNameChanged,
                nameField, true);

    }
};

Wf.OptionFieldsHelper.onNameChanged = function(type, args, scope) {
    var containers = Wf.editor.layer.containers;
    var newName = args[0];
    var pattern = /\S+/;
    if (!pattern.test(args[0])) {
        scope.setValue(scope.prevValue, false);
        Wf.Editor.messagePanel.setBody(i18n.get('EmptyName'));
        Wf.Editor.messagePanel.render("center");
        Wf.Editor.messagePanel.show();
        window.setTimeout(function(){Wf.Editor.messagePanel.hide()}, 3000);
        return;
    }
    for (var i = 0; i < containers.length; i++) {
        if (containers[i].optionsValue.name == newName) {
            scope.setValue(scope.prevValue, false);
            Wf.Editor.messagePanel.setBody(i18n.get('DuplicateName'));
            Wf.Editor.messagePanel.render("center");
            Wf.Editor.messagePanel.show();
            window.setTimeout(function(){Wf.Editor.messagePanel.hide()}, 3000);
            return;
        }
    }
    scope.prevValue=args[0];
};

Wf.OptionFieldsHelper.hideOptions = function(container) {
    if (container.optionsForm) {
        container.optionsValue = container.optionsForm.getValue();
        for (var i = 0 ; i < container.optionsForm.inputs.length ; i++) {
             var v = container.optionsForm.inputs[i];
             if (v.variableAliasInput!=null){
                var key = v.options.name;
                if(key) {
                    container.variables[key] = v.variableAliasInput.getValue();
                }
             }
        }
    }

    if (container.fields){
        for (var j = 0; j < container.fields.length; j++){
            var field = container.form.inputs[j];
            field.setContainer(container);
            if ("name" != container.fields[j].name){
                field.setVariableStyle();
            }
        }
    }

    container.optionsForm = null;
    var optionsParentEl = YAHOO.util.Dom.get("optionsForm");
    optionsParentEl.innerHTML = "";
};

Wf.OptionFieldsHelper.getVariables = function(container) {
    if (container.optionsForm) {
        for (var i = 0 ; i < container.optionsForm.inputs.length ; i++) {
            var v = container.optionsForm.inputs[i];
            if (v.variableAliasInput!=null){
                var key = v.options.name;
                if(key) {
                    var value = v.variableAliasInput.getValue();
                    container.variables[key] = value;
                }
            }
        }
    }
    for (key in container.variables) {
            if (container.variables[key]==null || container.variables[key] == ''){
                delete container.variables[key];
            }
        }
    return container.variables;
};

Wf.OptionFieldsHelper.setVariables = function(container, val) {
    if (!val) val = new Object();
        container.variables = new Object();
        for (key in val) {
             if (val[key]!=null){
                container.variables[key] = val[key];
             }
        }
        if (container.optionsForm) {
                for (var i = 0 ; i < container.optionsForm.inputs.length ; i++) {
                    var v = container.optionsForm.inputs[i];
                    if (v.variableAliasInput!=null){
                        var key = v.options.name;
                        if(key) {
                             v.variableAliasInput.setValue(container.variables[key]);
                        }
                    }
                }
            }
};

Wf.OptionFieldsHelper.getValue = function(container) {
    var optValue;
    var lang = YAHOO.lang;
    if (container.optionsForm) {
        optValue = container.optionsForm.getValue();
    } else if (container.optionsValue) {
        optValue = container.optionsValue;
        if (container.optFields) {
            for (var i = 0; i < container.optFields.length; i++) {
                var optFieldValue = container.optFields[i];
                if (lang.isUndefined(optValue[optFieldValue.name])) {
                    optValue[optFieldValue.name] = optFieldValue.value;
                }
            }
        }
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
     var newDiv = inputEx.cn('div',{className:'container'}), delButton, delDiv;

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
        delDiv = inputEx.cn('div', null, {width:'100%', textAlign:'right'}, null);
        delButton = inputEx.cn('a', {className: 'inputEx-List-link'}, null, this.options.listRemoveLabel);
        Event.addListener(delButton, 'click', this.onDelete, this, true);
        delDiv.appendChild(delButton);
        newDiv.appendChild(delDiv);
    } 

    // Line breaker
    newDiv.appendChild( inputEx.cn('div', null, {clear: "both"}) );

    this.childContainer.appendChild(newDiv);

    return el;
};

//Override ListField method to proper work with delete link wrapped by Div
inputEx.ListField.prototype.onDelete = function(e) {
	      
	   YAHOO.util.Event.stopEvent(e);
	   
	   // Prevent removing a field if already at minItems
	   if( YAHOO.lang.isNumber(this.options.minItems) && this.subFields.length <= this.options.minItems ) {
	      return;
	   }
	      
	   // Get the wrapping div element
	   var elementDiv = YAHOO.util.Event.getTarget(e).parentNode.parentNode;
	   
	   // Get the index of the subField
	   var index = -1;
	   
	   var subFieldEl = elementDiv.childNodes[this.options.useButtons ? 1 : 0];
	   for(var i = 0 ; i < this.subFields.length ; i++) {
	      if(this.subFields[i].getEl() == subFieldEl) {
	         index = i;
	         break;
	      }
	   }
	      
	   // Remove it
	   if(index != -1) {
	      this.removeElement(index);
	   }
		
		// Note: not very efficient
		this.resetAllNames();      
	
	   // Fire the updated event
	   this.fireUpdatedEvt();
};

//add wrapper to "select" element
inputEx.SelectField.prototype.renderComponent = function () {

    var i, length;

    // create DOM <select> node
    this.elWrap = inputEx.cn('div', {

        id: this.divEl.id ? this.divEl.id + '-field-wrap' : YAHOO.util.Dom.generateId(),
        name: this.options.wrapName || '',
        class: this.options.wrapClass || 'select-wrap'

    });
    this.el = inputEx.cn('select', {

        id: this.divEl.id ? this.divEl.id + '-field' : YAHOO.util.Dom.generateId(),
        name: this.options.name || ''

    });
    this.elWrap.appendChild(this.el);

    if(this.options.disabled) { this.el.disabled = 'disabled'; }

    // list of choices (e.g. [{ label: "France", value:"fr", node:<DOM-node>, visible:true }, {...}, ...])
    this.choicesList = [];

    // add choices
    for (i = 0, length = this.options.choices.length; i < length; i += 1) {
        this.addChoice(this.options.choices[i]);
    }

    // append <select> to DOM tree
    this.fieldContainer.appendChild(this.elWrap);
};