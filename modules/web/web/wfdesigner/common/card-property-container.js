
Wf.CardPropertyContainer = function(options, layer) {
    Wf.CardPropertyContainer.superclass.constructor.call(this, options, layer);
};

YAHOO.lang.extend(Wf.CardPropertyContainer, Wf.Container, {

      xtype: "Wf.CardPropertyContainer",
      preventSelfWiring: false,
      operationsField: null,

      cardEntityField: null,
      propertyPathField: null,

      valueField: null,
      dateField: null,
      lookupField: null,
      stringField: null,
      booleanField: null,

      activeField: null,

      isExpression: false,
      expressionField: null,
      expressionTestField: null,

      selectedAttributeType:null,

      valueWasUpdated:false,

      isLoadEvent:false,

      render: function() {
          Wf.CardPropertyContainer.superclass.render.call(this);
          for(var i = 0 ; i < this.form.inputs.length ; i++) {
          	    var field = this.form.inputs[i];
          		if ("operationType" == field.options.name){
          		   this.operationsField = field;
          		}
                if ("value" == field.options.name){
          		    this.valueField = field;
          		}
          		if ("useExpresssion" == field.options.name){
                	this.expressionField = field;
                }
                if ("expressionTest" == field.options.name){
                    this.expressionTestField = field;
                }
                if ("cardClass" == field.options.name){
                    this.cardEntityField = field;
                }
                if ("propertyPath" == field.options.name){
                    this.propertyPathField = field;
                }
          }
          this.prepareLookupField();
          this.prepareDateField();
          this.prepareStringField();
          this.prepareBooleanField();
          this.prepareExpressionField();
          this.valueField.hide();
          this.expressionTestField.hide();
          this.hideAllFields();
      },

      onUpdateForm: function(type,args){
          Wf.CardPropertyContainer.superclass.onUpdateForm.call(this,type,args);
          this.redrawAllContainerWires();
      },

      redrawAllContainerWires:function(){
           for (var i=0;i<this.terminals.length;i++){
               this.terminals[i].redrawAllWires();
           }
      },

      hideAllFields:function() {
          this.lookupField.hide();
          this.dateField.hide();
          this.stringField.hide();
          this.booleanField.hide();
          this.expressionField.hide();
      },

      prepareLookupField:function() {
          var initialParams = new Object();
          initialParams.label = i18nDict.value;
          initialParams.name = "lookupValue";
          initialParams.type = "string";
          initialParams.container = this;
          this.lookupField = new Wf.CardEntityField(initialParams);
          this.addCustomFields(this.lookupField);
      },

      prepareDateField:function() {
          var initialParams = new Object();
          initialParams.label = i18nDict.value;
          initialParams.name = "dateValue";
          initialParams.type = "wfDateTimeField";
          this.dateField = new Wf.DateTimeField(initialParams);
          this.addCustomFields(this.dateField);
      },

      compareArrays: function (array1,array2) {
          // if the other array is a falsy value, return
          if (!array2)
              return false;

          if (!array1)
              return false;

          // compare lengths - can save a lot of time
          if (array1.length != array2.length)
              return false;

          for (var i = 0, l=array1.length; i < l; i++) {
              // Check if we have nested arrays
              if (array1[i] instanceof Array && array2[i] instanceof Array) {
                  // recurse into the nested arrays
                  if (!array1[i].equals(array2[i]))
                      return false;
              }
              else if (array1[i] != array2[i]) {
                  // Warning - two different object instances will never be equal: {x:20} != {x:20}
                  return false;
              }
          }
          return true;
      },

      addCustomFields:function(field){
          this.form.inputs[this.form.inputs.length]=field;
          this.form.fieldset.appendChild(field.getEl());
          field.updatedEvt.subscribe(this.setValueToValueField, this, !this.isLoadEvent);
      },

      prepareStringField:function() {
          var initialParams = new Object();
          initialParams.label = i18nDict.value;
          initialParams.name = "stringValue";
          initialParams.type = "string";
          this.stringField = new inputEx.StringField(initialParams);
          this.addCustomFields(this.stringField);
      },

      prepareExpressionField:function() {
          if (this.expressionField.getValue()){
              this.isExpression = true;
          }
          this.expressionField.updatedEvt.subscribe(this.showExpressionField, this, true,"");
      },

      showExpressionField:function(event,result,value){
          if (value instanceof Wf.CardPropertyContainer){
              value = "";
          }
          if ("DATE_TIME"==this.selectedAttributeType||"DATE"==this.selectedAttributeType){
                if (this.expressionField.getValue()){
                   this.isExpression = true;
                   this.activeField = this.stringField;
                   this.dateField.hide();
                   this.updateValue(value);
                   this.activeField.setValue(value, !this.isLoadEvent);
                } else {
                   this.isExpression = false;
                   this.stringField.hide();
                   this.activeField = this.dateField;
                   var fieldValue = this.activeField.getValue();
                   if (fieldValue!=null){
                        this.updateValue(fieldValue.getTime(),true) ;
                   }
                   else {
                       this.updateValue(null,true) ;
                   }

                }
                this.activeField.show();
          } else if (this.expressionField.getValue()) {
               this.isExpression = true;
               if (this.activeField)
                    this.activeField.hide();
               this.activeField = this.stringField;
               this.activeField.show();
               this.updateValue(value);
               this.activeField.setValue(value, !this.isLoadEvent);
          } else {
               this.isExpression = false;
               if (this.activeField === this.booleanField) {
                    this.updateValue(false);
               } else {
                    this.updateValue("");
               }
          }

      },


      prepareBooleanField:function() {
          var initialParams = new Object();
          initialParams.label = i18nDict.value;
          initialParams.name = "booleanValue";
          initialParams.type = "boolean";
          this.booleanField = new inputEx.CheckBox(initialParams);
          this.addCustomFields(this.booleanField);
      },

      updateValue: function(value, sendUpdatedEvt){
          this.valueField.setValue(value, !this.isLoadEvent && sendUpdatedEvt !== false);
          if (this.isExpression){
             var value_to_send = encodeURIComponent(value);
             var isLoadEvent = this.isLoadEvent;
             YAHOO.util.Connect.asyncRequest(
                'GET',
                'action/checkExpression.json?value=' + value_to_send+'&type='+this.selectedAttributeType,
                {
                    success: function(o) {
                         var r = o.responseText;
                         o.argument[0].expressionTestField.setValue(r, !isLoadEvent && sendUpdatedEvt);
                         o.argument[0].optionsValue[o.argument[0].expressionTestField.options.name] = o.argument[0].expressionTestField.getValue();
                    },
                    failure: function(o) {
                         var error = o.status + " " + o.statusText;
                         console.log(error);
                    },
                    argument: [this]
                },
                null
                );
          }
      },

      setValueToValueField: function(event,result){
          var value = result[0];
          if (result[1] instanceof Wf.DateTimeField){
             value = value.getTime();
          }
          this.updateValue(value);
      },

      updateContainer: function(result, isLoadEvent){
          this.isLoadEvent = isLoadEvent;
          try {
              this.updateOperationsField(result.ops, isLoadEvent);
              this.hideAllFields();
              this.updateValuefields(result, isLoadEvent);
          } finally {
              if (this.isLoadEvent) {
                    this.isLoadEvent = false;
              }
          }
      },

      updateOperationsField: function(ops){
          if (this.operationsField!=null){
              var value = this.operationsField.getValue();
              if (!this.compareArrays(this.operationsField.choicesList,ops)){
                    this.operationsField.setCustomChoises(ops);
                    this.operationsField.setValue(value,!this.isLoadEvent);
              }
          }
      },

      setVisibleActiveField: function(visible){
          if (this.activeField==null) return;
          if (visible){
                this.activeField.show();
          }
          else {
                this.activeField.hide();
          }
      },

      updateValuefields: function(result){
          if (this.selectedAttributeType==null){
              this.valueWasUpdated = false;
          }
          else {
              this.valueWasUpdated = this.selectedAttributeType != result.attributeType;
          }
          this.selectedAttributeType = result.attributeType;
          this.expressionTestField.setValue("", !this.isLoadEvent)
          if (this.valueWasUpdated){
                this.expressionField.setValue(false, !this.isLoadEvent);
                this.isExpression = false;
                this.updateValue(null, !this.isLoadEvent);
                this.expressionField.hide();
          }
          if (this.expressionField.getValue()){
              this.showExpressionField(null,null,this.valueField.getValue());
              this.expressionField.show();
          }
          else {
            if ("ENTITY"==result.attributeType||"ENUM"==result.attributeType){
                    this.lookupField.show();
                    this.activeField = this.lookupField;
                    var i;

                    this.lookupField.clear();
                    this.lookupField.setClazz(result.clazz);
                    for (var j = 0; j < result.values.length; j++) {
                        var s = result.values[j];
                        if (this.lookupField.getChoicePosition(s) == -1)
                            this.lookupField.addChoice(s);
                    }

                    if (typeof(this.lookupField.doSortChoice) == 'function') {
                        this.lookupField.doSortChoice();
                    }
                    if (this.isLoadEvent || this.lookupField.containsChoice(this.valueField.getValue())) {
                        this.lookupField.setValue(this.valueField.getValue(), !this.isLoadEvent);
                    } else {
                        this.activeField.setValue('', true);
                    }
          } else if ("DATE_TIME"==result.attributeType||"DATE"==result.attributeType){
                this.dateField.show();
                this.activeField = this.dateField;
                if (this.valueField.getValue()!=""){
                    this.dateField.setValue(new Date(this.valueField.getValue()*1), !this.isLoadEvent);
                }
                else {
                    this.dateField.setValue(new Date(), !this.isLoadEvent);
                }
                this.expressionField.show();
          } else if ("BOOLEAN"==result.attributeType){
                this.booleanField.show();
                this.activeField = this.booleanField;
                this.booleanField.setValue(this.valueField.getValue() ? this.valueField.getValue() : false, !this.isLoadEvent);
          } else {
                this.stringField.show();
                this.activeField = this.stringField;
                this.stringField.setValue(this.valueField.getValue(), !this.isLoadEvent);
                this.expressionField.show();
          }
          if (this.activeField && this.operationsField) {
                if (this.operationsField.isVisibleActiveField()) {
                    this.activeField.show();
                } else {
                    this.activeField.hide();
                }
          }
          }
          this.redrawAllContainerWires();
      }
});