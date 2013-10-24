/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.12.10 11:06
 *
 * $Id$
 */

/**
 * CubaAdapter. Expect JSON response for all queries.
 * @class WireIt.WiringEditor.adapters.WfAdapter
 */
WireIt.WiringEditor.adapters.WfAdapter = {

	/**
	 * You can configure this adapter to different schemas.
	 * TIP: "url" can be a function !
	 */
	config: {
		saveWiring: {
			method: 'POST',
			url: function() {
                return "action/save.json" + Wf.createProcIdParam();
            }
		},
		deleteWiring: {
			method: 'DELETE',
			url: 'action/delete.json'
		},
		listWirings: {
			method: 'GET',
			url: function() {
                return "action/load.json" + Wf.createProcIdParam();
            }
		}
	},

	/**
	 * init the adapter
	 * @method init
	 * @static
	 */
	init: function() {
		YAHOO.util.Connect.setDefaultPostHeader('application/json');
	},

	/**
	 * called when saved
	 * @method saveWiring
	 * @static
	 */
	saveWiring: function(val, callbacks) {
		this._sendRequest("saveWiring", val, callbacks);
	},

	/**
	 * called when deleted
	 * @method deleteWiring
	 * @static
	 */
	deleteWiring: function(val, callbacks) {
		this._sendRequest("deleteWiring", val, callbacks);
	},

	/**
	 * called to load the wirings
	 * @method listWirings
	 * @static
	 */
	listWirings: function(val, callbacks) {
		this._sendRequest("listWirings", val, callbacks);
	},

	/**
	 * send a request in JSON
	 * @method _sendRequest
	 * @static
	 */
	_sendRequest: function(action, value, callbacks) {
        var postData = YAHOO.lang.JSON.stringify(value);

        var u = "";
        if (YAHOO.lang.isFunction(this.config[action].url))
            u = this.config[action].url();
        else
            u = this.config[action].url;

        YAHOO.util.Connect.initHeader("Content-Type", "application/x-www-form-urlencoded");
        YAHOO.util.Connect.asyncRequest(
                this.config[action].method,
                u,
                {
                    success: function(o) {
                        var s = o.responseText, r = YAHOO.lang.JSON.parse(s);
                        Wf.processWiringOnLoad(r);
                        callbacks.success.call(callbacks.scope, r);
                        Wf.loadScripts(function(val) {
                            Wf.scriptsCache = val;
                        }, null);
                        Wf.loadSubDesigns(function(val) {
                            Wf.subdesignCache = val;
                         }, null);
                    },
                    failure: function(o) {
                        var error = o.status + " " + o.statusText;
                        callbacks.failure.call(callbacks.scope, error);
                    }
                },
                postData
        );
    }

};
