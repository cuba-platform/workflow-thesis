/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 20.12.10 11:06
 *
 * $Id$
 */

var wfLanguage = {

    languageName: "Workflow",

    propertiesFields: [
        // default fields (the "name" field is required by the WiringEditor):
        {"type": "string", "name": "name", label: "msg://name"}
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
                "optFields": [
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
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
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
                "optFields": [
                    {
                        "type": "string",
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
                "width": 230,

                "fields": [
                    {
                        "type": "string",
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "CardState"
                    }
                ],

                "terminals": [
                    {
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 100, "top": -15},
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "name": "out", "direction": [0,1], "offsetPosition": {"left": 100, "bottom": -15},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
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
                "width": 230,

                "fields": [
                    {
                        "type": "string",
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "msg://Assignment"
                    },
                    {
                        "type": "string",
                        "name": "role",
                        "label": "msg://role",
                        "required": false,
                        "value": ""
                    }
                ],

                "optFields": [
                    {
                        "type": "string",
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
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 100, "top": -15},
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
                        "name": "name",
                        "label": "msg://name",
                        "required": false,
                        "value": "msg://ParallelAssignment"
                    },
                    {
                        "type": "string",
                        "name": "role",
                        "label": "msg://role",
                        "required": false,
                        "value": ""
                    }
                ],

                "optFields": [
                    {
                        "type": "string",
                        "label": "msg://description",
                        "name": "description",
                        "value": ""
                    },
                    {
                        "type": "wfOutputSelect",
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
                        "name": "in", "direction": [0,-1], "offsetPosition": {"left": 100, "top": -15},
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
                "optFields": [
                    {
                        "type": "string",
                        "label": "msg://name",
                        "name": "name",
                        "value": ""
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
                        "direction": [0,-1], "offsetPosition": {"left": 7, "top": -17 }, "name": "in",
                        "ddConfig": {"type": "in","allowedTypes": ["out"]}
                    },
                    {
                        "direction": [-1,0], "offsetPosition": {"left": -15, "top": 5 }, "name": "yes",
                        "label": "msg://Decision.yes", "labelPosition": {"left": -15, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
                    },
                    {
                        "direction": [1,0], "offsetPosition": {"left": 29, "top": 5 }, "name": "no",
                        "label": "msg://Decision.no", "labelPosition": {"left": 40, "top": 25},
                        "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true
                    }
                ]
            }
        }
    ]
};