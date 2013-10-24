<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="chrome=1">
    <title><#if locale == "ru">Дизайнер процессов CUBA<#else>CUBA Workflow Designer</#if></title>

    <#include "../common/main-head.ftl">

    <script type="text/javascript" src="../common/lang.js"></script>
    <script type="text/javascript" src="main.js"></script>
</head>

<body class="yui-skin-sam">

<div id="top">
    <div class="logo i18n">CUBA Workflow Designer</div>
    <div id="toolbar"></div>
</div>


<div id="left">

    <ul id="modulesAccordionView">
        <li>
            <h2 class="i18n">Common</h2>

            <div>
                <div id='module-category-common'></div>
            </div>
        </li>
    </ul>

</div>

<div id="right">
    <ul id="accordionView">
        <li>
            <h2 class="i18n">Process</h2>

            <div>
                <div id="propertiesForm"></div>
            </div>
        </li>
        <li>
            <h2 class="i18n">Module</h2>

            <div>
                <div id="optionsForm"></div>
            </div>
        </li>
        <li>
            <h2 class="i18n">Minimap</h2>

            <div style='position: relative;'>
                <div id="layerMap"></div>
            </div>
        </li>
        <!--<li>-->
            <!--<h2>Infos</h2>-->

            <!--<div>-->
                <!--<div style="padding: 10px;">-->
                    <!--<p>This example shows how to use the <i>ImageContainer</i> and <i>FormContainer</i> in a language-->
                        <!--definition.</p>-->
                    <!--<br/>-->

                    <!--<p><b>Drag and drop modules from the Module list</b> on the left to the working layer in the middle.-->
                    <!--</p>-->
                    <!--<br/>-->

                    <!--<p><a href="demo.js" target="_new">Click here to view the language definition for this editor.</a>-->
                    <!--</p>-->
                <!--</div>-->
            <!--</div>-->
        <!--</li>-->
    </ul>
</div>

<div id="center">
</div>


<div id="helpPanel">
    <div class="hd i18n">Welcome to the CUBA Workflow Designer</div>
    <div class="bd" style="text-align: left;">
        <p><#if locale == "ru">
            Перетащите мышью модули из левой части редактора в рабочую область.
            <#else>
            Drag and drop modules from the Module list on the left to the working layer in the middle.
        </#if></p>
        <br/>
    </div>
</div>

</body>
</html>