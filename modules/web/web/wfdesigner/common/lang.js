/*
 * Copyright (c) 2008-2014 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

var wfLanguage = {

    languageName: "Workflow",

    propertiesFields: [
        // default fields (the "name" field is required by the WiringEditor):
        {"type": "string", "name": "name", label: "msg://name", "trim": true}
    ],

    modules: [
        {
            "name": "Start",
            "label": "msg://Start",
            "category": "common",
            "container": {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Bubble",
                "icon": "../common/res/icons/start_icon.png",
                "image": "../common/res/icons/start.png",
                "selectedImage": "../common/res/icons/start-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "value": "msg://Start",
                        "readonly":"true"
                    },
                    {
                        type: "group",
                        legend: "msg://forms",
                        collapsible: "true",
                        collapsed: "true",
                        name: "forms",
                        fields:[
                            {
                                type: "list",
                                name: "list",
                                elementType: {
                                    type: "wfFormSelect",
                                    label: "msg://form",
                                    name: "form",
                                    useTransition: false
                                }
                            }
                        ]
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,1], "offsetPosition": {"left": 8, "top": 30 }, "name": "out",
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    }
                ]
            }
        },
        {
            "name": "End",
            "label": "msg://End",
            "category": "common",
            "container": {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Bubble",
                "icon": "../common/res/icons/end_icon.png",
                "image": "../common/res/icons/end.png",
                "selectedImage": "../common/res/icons/end-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "required": false,
                        "value": "msg://End"
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15}, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    }
                ]
            }
        },
        {
            "name": "CardState",
            "label": "msg://CardState",
            "description": "msg://CardState.descr",
            "category": "common",
            "container": {
                "xtype": "Wf.Container",
                "icon": "../common/res/icons/card_state_icon.png",
                "width": 250,

                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "required": false,
                        "value": "msg://CardState",
                        "readonly":"true"
                    }
                ],

                "fields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "CardState"
                    }
                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "name": "out", "direction": [0,1], "offsetPosition": {"left": 112, "bottom": -15},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    }
                ]
            }
        },
        {
            "name": "Assignment",
            "label": "msg://Assignment",
            "description": "msg://Assignment",
            "category": "common",
            "container": {
                "xtype": "Wf.MultiOutContainer",
                "icon": "../common/res/icons/assignment_icon.png",
                "width": 250,

                "fields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "msg://Assignment",
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "name": "role",
                        "label": "msg://role",
                        "required": false,
                        "value": ""
                    }
                ],

                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "required": false,
                        "value": "msg://Assignment",
                        "readonly":"true"
                    },

                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://description",
                        "name": "description",
                        "required": false,
                        "value": ""
                    },
                    {
                        type: "group",
                        legend: "msg://forms",
                        collapsible: "true",
                        collapsed: "true",
                        name: "forms",
                        fields:[
                            {
                                type: "list",
                                name: "list",
                                elementType: {
                                    type: "wfFormSelect",
                                    label: "msg://form",
                                    name: "form",
                                    useTransition: true
                                }
                            }
                        ]
                    },
                    {
                        type: "group",
                        legend: "msg://timers",
                        collapsible: "true",
                        collapsed: "true",
                        name: "timers",
                        fields:[
                            {
                                type: "list",
                                name: "list",
                                elementType: {type: "wfTimerSelect", label: "msg://timer", name: "timer"}
                            }
                        ]
                    }
                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    }
                ]
            }
        },
        {
            "name": "ParallelAssignment",
            "label": "msg://ParallelAssignment",
            "description": "msg://ParallelAssignment",
            "category": "common",
            "container": {
                "xtype": "Wf.MultiOutContainer",
                "icon": "../common/res/icons/parallel_assignment_icon.png",
                "width": 250,

                "fields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "msg://ParallelAssignment"
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "name": "role",
                        "label": "msg://role",
                        "required": false,
                        "value": ""
                    }
                ],

                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "required": false,
                        "value": "msg://ParallelAssignment",
                        "readonly":"true"
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://description",
                        "name": "description",
                        "value": ""
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://ParallelAssignment.successTransition",
                        "name": "successTransition",
                        "value": ""
                    },
                    {
                        "type": "boolean",
                        "label": "msg://ParallelAssignment.refusedOnly",
                        "name": "refusedOnly",
                        "value": false
                    },
                    {
                        "type": "boolean",
                        "label": "msg://ParallelAssignment.finishBySingleUser",
                        "name": "finishBySingleUser",
                        "value": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://ParallelAssignment.statusesForFinish",
                        "name": "statusesForFinish",
                         "value": ""
                    },
                    {
                        type: "group",
                        legend: "msg://forms",
                        collapsible: "true",
                        collapsed: "true",
                        name: "forms",
                        fields:[
                            {
                                type: "list",
                                name: "list",
                                elementType: {
                                    type: "wfFormSelect",
                                    label: "msg://form",
                                    name: "form",
                                    useTransition: true
                                }
                            }
                        ]
                    },
                    {
                        type: "group",
                        legend: "msg://timers",
                        collapsible: "true",
                        collapsed: "true",
                        name: "timers",
                        fields:[
                            {
                                type: "list",
                                name: "list",
                                elementType: {type: "wfTimerSelect", label: "msg://timer", name: "timer"}
                            }
                        ]
                    }
                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    }
                ]
            }
        },
        {
            "name": "Decision",
            "label": "msg://Decision",
            "category": "common",
            "container": {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Wf-Decision",
                "icon": "../common/res/icons/decision_icon.png",
                "image": "../common/res/icons/decision.png",
                "selectedImage": "../common/res/icons/decision-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "value": "msg://Decision"
                    },
                    {
                        "type": "wfScriptSelect",
                        "label": "msg://script",
                        "name": "script",
                        "value": ""
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "direction": [1,0], "offsetPosition": {"right": -14, "top": 8 }, "name": "yes",
                        "label": "msg://Decision.yes", "labelPosition": {"right": -18, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    },
                    {
                        "direction": [-1,0], "offsetPosition": {"left": -14, "top": 8 }, "name": "no",
                        "label": "msg://Decision.no", "labelPosition": {"left": -18, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    }
                ]
            }
        },
        {
            "name" : "Fork",
            "label" : "msg://Fork",
            "category" : "common",
            "container" : {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Wf-Fork",
                "icon": "../common/res/icons/fork_icon.png",
                "image": "../common/res/icons/fork.png",
                "selectedImage": "../common/res/icons/fork-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "value": "msg://Fork"
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "direction": [-1,0], "offsetPosition": {"left": -14, "top": 8 }, "name": "out1",

                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
                    },
                    {
                        "direction": [1,0], "offsetPosition": {"right": -14, "top": 8 }, "name": "out2",

                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
                    },
                    {
                        "direction": [0,1], "offsetPosition": {"left": 8, "bottom": -15 }, "name": "out3",

                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
                    }
                ]
            }
        },
        {
            "name" : "Join",
            "label" : "msg://Join",
            "category" : "common",
            "container" : {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Wf-Join",
                "icon": "../common/res/icons/join_icon.png",
                "image": "../common/res/icons/join.png",
                "selectedImage": "../common/res/icons/join-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "value": "msg://Join"
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://role",
                        "name": "role"
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,1], "offsetPosition": {"left": 8, "bottom": -15 }, "name": "out",
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    },
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in1",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    }
                ]
            }
        },

        {
            "name": "IsRoleAssigned",
            "label": "msg://IsRoleAssigned",
            "category": "common",
            "container": {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Wf-IsRoleAssigned",
                "icon": "../common/res/icons/isRoleAssigned_icon.png",
                "image": "../common/res/icons/isRoleAssigned.png",
                "selectedImage": "../common/res/icons/isRoleAssigned-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://name",
                        "name": "name",
                        "value": "msg://IsRoleAssigned"
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label": "msg://role",
                        "name": "roleKey",
                        "value": "msg://role"
                    }
                ],
                "terminals": [
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "direction": [-1,0], "offsetPosition": {"left": -14, "bottom": 8 }, "name": "no",
                        "label": "msg://Decision.no", "labelPosition": {"left": -18, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    },
                    {
                        "direction": [1,0], "offsetPosition": {"right": -14, "top": 8 }, "name": "yes",
                        "label": "msg://Decision.yes", "labelPosition": {"right": -20, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
                    }
                ]
            }
        },
        {
             "name": "SubDesign",
             "label": "msg://SubDesign",
             "description": "msg://SubDesign",
             "category": "common",
             "container": {
                 "xtype": "Wf.DesignContainer",
                 "icon": "../common/res/icons/assignment_icon.png",
                 "width": 250,

                 "optFields": [
                     {
                          "type": "string",
                        "trim": true,
                          "label":"msg://name",
                          "name":"name",
                          "required":false,
                          "value":"msg://SubDesign",
                          "readonly":"true"
                     },
                     {
                          "type": "wfDesignSelect",
                          "name": "design",
                          "label": "msg://SubDesign",
                          "required": false,
                          "disabled": true
                     }
                 ],
                 "fields": [
                     {
                          "type": "string",
                        "trim": true,
                          "name": "name",
                          "label": "msg://name",
                          "required": false
                     },
                     {
                          "type": "wfDesignSelect",
                          "name": "design",
                          "label": "msg://SubDesign",
                          "required": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "name": "params",
                        "label": "msg://params",
                        "required": false
                    },
                 ],

                 "terminals": [
                     {
                          "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                          "ddConfig": {"type": "in","allowedTypes": ["out"]}
                     },
                     {
                          "name": "out", "direction": [0,1], "offsetPosition": {"left": 112, "bottom": -15},
                          "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true,
                          "nMaxWires" : 1
                     }
                 ]
             }
        },
        {
            "name": "ReadCardProperty",
            "label":"msg://ReadCardProperty",
            "category":"eventProcess",
            "container": {
                "xtype":"Wf.CardPropertyContainer",
                "icon":"../common/res/icons/card_state_icon.png",
                "width":264,
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://name",
                        "name":"name",
                        "required":false,
                        "value":"msg://ReadCardProperty",
                        "disabled":"true"
                    },
                    {
                        "type":"cardInheritorSelect",
                        "label":"msg://cardInheritorSelect",
                        "name":"cardClass",
                        "required":false,
                        "disabled": true
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://propertyPath",
                        "name":"propertyPath",
                        "required":false,
                        "disabled": true,
                        "allowParameter": false
                    },
                    {
                        "type":"operationTypeSelect",
                        "label":"msg://operationType",
                        "name":"operationType",
                        "required":false,
                        "disabled": true
                    },
                    {
                        "type": "boolean",
                        "name": "useExpresssion",
                        "label": "msg://useExpresssion",
                        "required": false,
                        "disabled": true,
                        "value": false,
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://value",
                        "name":"value",
                        "required":false,
                        "value":"",
                        "disabled": true
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://expressionTest",
                        "name":"expressionTest",
                        "required":false,
                        "disabled": true,
                        "value":"",
                        "allowParameter": false
                    }

                ],
                "fields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "required": false
                    },
                    {
                        "type":"cardInheritorSelect",
                        "label":"msg://cardInheritorSelect",
                        "name":"cardClass",
                        "required":true,
                    },
                    {
                        "type":"cardPropertyField",
                        "label":"msg://propertyPath",
                        "name":"propertyPath",
                        "required":false
                    },
                    {
                        "type":"operationTypeSelect",
                        "label":"msg://operationType",
                        "name":"operationType",
                        "required":false
                    },
                    {
                        "type": "boolean",
                        "name": "useExpresssion",
                        "label": "msg://useExpresssion",
                        "required": false,
                        "value": false,
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://expressionTest",
                        "name":"expressionTest",
                        "required":false,
                        "disabled": true,
                        "value":"",
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://value",
                        "name":"value",
                        "required":false,
                        "value":""
                    }
                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "name": "Yes", "direction": [0,1], "offsetPosition": {"left": 50, "bottom": -15},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true,
                        "label": "msg://Decision.yes", "labelPosition": {"left": 30, "bottom": -20},
                        "nMaxWires" : 1
                    },
                    {
                        "name": "No", "direction": [0,1], "offsetPosition": {"left": 170, "bottom": -15},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true,
                        "label": "msg://Decision.no", "labelPosition": {"left": 190, "bottom": -20},
                        "nMaxWires" : 1
                    }
                ]
            }
        },
        {
            "name": "WriteCardProperty",
            "label":"msg://WriteCardProperty",
            "category":"eventProcess",
            "container": {
                "xtype":"Wf.CardPropertyContainer",
                "icon":"../common/res/icons/card_state_icon.png",
                "width":251,
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://name",
                        "name":"name",
                        "required":false,
                        "value":"msg://WriteCardProperty",
                        "disabled":"true"
                    },
                    {
                        "type":"cardInheritorSelect",
                        "label":"msg://cardInheritorSelect",
                        "name":"cardClass",
                        "required":false,
                        "disabled": true
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://propertyPath",
                        "name":"propertyPath",
                        "required":false,
                        "disabled": true,
                        "allowParameter": false
                    },
                    {
                        "type": "boolean",
                        "name": "useExpresssion",
                        "label": "msg://useExpresssion",
                        "required": false,
                        "disabled": true,
                        "value": false,
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://value",
                        "name":"value",
                        "required":false,
                        "value":"",
                        "disabled": true
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://expressionTest",
                        "name":"expressionTest",
                        "required":false,
                        "disabled": true,
                        "value":"",
                        "allowParameter": false
                    }

                ],
                "fields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "required": false
                    },
                    {
                        "type":"cardInheritorSelect",
                        "label":"msg://cardInheritorSelect",
                        "name":"cardClass",
                        "required":true,
                    },
                    {
                        "type":"cardPropertyField",
                        "label":"msg://propertyPath",
                        "name":"propertyPath",
                        "required":false
                    },
                    {
                        "type": "boolean",
                        "name": "useExpresssion",
                        "label": "msg://useExpresssion",
                        "required": false,
                        "value": false,
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://expressionTest",
                        "name":"expressionTest",
                        "required":false,
                        "disabled": true,
                        "value":"",
                        "allowParameter": false
                    },
                    {
                        "type": "string",
                        "trim": true,
                        "label":"msg://value",
                        "name":"value",
                        "required":false,
                        "value":""
                    }

                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 112, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "name": "out", "direction": [0,1], "offsetPosition": {"left": 112, "bottom": -15},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true,
                        "nMaxWires" : 1
                    }
                ]
            }
        },
        {
            "name": "EmptyModule",
            "label": "msg://EmptyModule",
            "category": "common",
            "container": {
                "xtype":"Wf.ImageContainer",
                "className": "WireIt-Container WireIt-ImageContainer Wf-EmptyModule",
                "icon": "../common/res/icons/emptyModule_icon.png",
                "image": "../common/res/icons/emptyModule.png",
                "selectedImage": "../common/res/icons/emptyModule-selected.png",
                "optFields": [
                    {
                        "type": "string",
                        "trim": true,
                        "name": "name",
                        "label": "msg://name",
                        "value": "msg://EmptyModule"
                    }

                ],
                "terminals": [
                    {
                        "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "direction": [0,1], "offsetPosition": {"left": 8, "bottom": -15 }, "name": "out",
                        "ddConfig": {"type": "out","allowedTypes": ["in"]},"alwaysSrc": true, "nMaxWires": 1
                    }
                ]
            }
        }

    ]
};
