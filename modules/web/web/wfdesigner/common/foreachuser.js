/*
 * Copyright (c) 2015 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 */

var forEachUserModule = {
    "name" : "ForEachUser",
    "label" : "msg://ForEachUser",
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
                "label": "msg://name",
                "name": "name",
                "value": "msg://ForEachUser"
            },
            {
                "type": "string",
                "name": "role",
                "label": "msg://role",
                "required": true,
                "value": ""
            }
        ],
        "terminals": [
            {
                "direction": [0,-1], "offsetPosition": {"left": 8, "top": -15 }, "name": "in",
                "ddConfig": {"type": "in","allowedTypes": ["out"]}
            },
            {
                "direction": [0,1], "offsetPosition": {"left": 8, "bottom": -15}, "name": "out",
                "ddConfig": {"type": "out","allowedTypes": ["in"]}, "alwaysSrc": true, "nMaxWires": 1
            }
        ]
    }
};
wfLanguage.modules.push(forEachUserModule);