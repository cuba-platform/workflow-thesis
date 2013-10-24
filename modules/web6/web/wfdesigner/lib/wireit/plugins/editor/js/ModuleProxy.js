(function() {
    var util = YAHOO.util,lang = YAHOO.lang, Dom = util.Dom;


/**
 * Module Proxy handle the drag/dropping from the module list to the layer (in the WiringEditor)
 * @class ModuleProxy
 * @constructor
 * @param {HTMLElement} el
 * @param {WireIt.WiringEditor} WiringEditor
 */
WireIt.ModuleProxy = function(el, WiringEditor) {
   
   this._WiringEditor = WiringEditor;
   
   // Init the DDProxy
   WireIt.ModuleProxy.superclass.constructor.call(this,el, "module", {
        dragElId: "moduleProxy"
    });
    
    this.isTarget = false; 
};
lang.extend(WireIt.ModuleProxy,YAHOO.util.DDProxy, {

    onDrag: function(e) {
        var layer = this._WiringEditor.layer;
        var clientWidth = layer.parentEl.clientWidth + YAHOO.util.Dom.get("left").clientWidth;
        var clientHeight = layer.parentEl.clientHeight + YAHOO.util.Dom.get("top").clientHeight;
        var clientRects = this.getDragEl().getClientRects()[0];
        var elPosX = clientRects.left;
        var elPosY = clientRects.top;
        var width = clientRects.width;
        var height = clientRects.height;

        if (elPosX + width > clientWidth) {
            WireIt.sn(this.getDragEl(), null, { left:clientWidth - width + 'px' });
        }
        if (elPosY + height > clientHeight) {
            WireIt.sn(this.getDragEl(), null, {top: clientHeight - height + 'px'});
        }
    },

   /**
    * copy the html and apply selected classes
    * @method startDrag
    */
   startDrag: function(e) {
      WireIt.ModuleProxy.superclass.startDrag.call(this,e);
       var del = this.getDragEl(),
			  lel = this.getEl();
       del.innerHTML = lel.innerHTML;
       del.className = lel.className;
   },
   
   /**
    * Override default behavior of DDProxy
    * @method endDrag
    */
   endDrag: function(e) {},
    
   /**
    * Add the module to the WiringEditor on drop on layer
    * @method onDragDrop
    */
   onDragDrop: function(e, ddTargets) { 
      // The layer is the only target :
      var layerTarget = ddTargets[0],
			 layer = ddTargets[0]._layer,
			 del = this.getDragEl(),
			 pos = Dom.getXY(del),
			 layerPos = Dom.getXY(layer.el);
      this._WiringEditor.addModule( this._module ,[pos[0]-layerPos[0]+layer.el.scrollLeft, pos[1]-layerPos[1]+layer.el.scrollTop]);
    }
   
});

})();
