
Wf.registerForms = (function() {
    var originalRegisterForms = Wf.registerForms;
    return function() {
        originalRegisterForms();
        Wf.FormSelect.registerForm("subProc", {
            label: i18n.get("subProcForm"),
            fields: [
                {type: "boolean", name: "commentVisible", label: i18n.get("commentVisible")},
                {type: "boolean", name: "commentRequired", label: i18n.get("commentRequired")},
                {type: "boolean", name: "dueDateVisible", label: i18n.get("dueDateVisible")},
                {type: "boolean", name: "cardRolesVisible", label: i18n.get("cardRolesVisible")},
                {type: "WfJbpmProcSelect", name: "subProcCode", label: i18n.get("SubProc.proc")},
                {type: "string", name: "requiredRoles", label: i18n.get("requiredRoles")},
                {type: "string", name: "visibleRoles", label: i18n.get("visibleRoles")}
            ]
        });
    }
})();

var subProcModule = {
    "name": "SubProc",
    "label": "msg://SubProc",
    "description": "msg://SubProc",
    "category": "common",
    "container": {
        "xtype": "Wf.Container",
        "icon": "../common/res/icons/assignment_icon.png",
        "width": 320,

        "fields": [
            {
                "type": "string",
                "name": "name",
                "label": "msg://name",
                "required": false,
                "value": "msg://SubProc"
            },
            {
                "type": "WfJbpmProcSelect",
                "name": "subProcCode",
                "label": "msg://SubProc.proc",
                "required": true
            }
        ],

        "optFields": [
            {
                "label": "msg://name",
                "type": "string",
                "name": "name",
                "required": false,
                "value": "msg://SubProc",
                "readonly":"true"
            },
            {
                "type": "string",
                "label": "msg://description",
                "name": "description",
                "required": false,
                "value": ""
            }
        ],

        "terminals": [
            {
                "name": "in", "direction": [0,-1], "offsetPosition": {"left": 152, "top": -15},
                "ddConfig": {"type": "in","allowedTypes": ["out"]}
            },
            {
                "direction": [0,1], "offsetPosition": {"left": 152, "bottom": -15 }, "name": "subProcCompleted",
                "label": "msg://SubProc.subProcCompleted", "labelPosition": {"left": 172, "bottom": -15},
                "ddConfig": {"type": "out","allowedTypes": ["in"]},"alwaysSrc": true, "nMaxWires": 1
            }
        ]
    }
};
wfLanguage.modules.push(subProcModule);