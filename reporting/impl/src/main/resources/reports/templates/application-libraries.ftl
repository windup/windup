<!DOCTYPE html>

<html lang="en">
    <#if reportModel.applicationReportIndexModel??>
        <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
    </#if>
    <#if reportModel.projectModel??>
    	<script>
    		var showExternalJars = true;
    	</script>
    </#if>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <#--<title>-->
            <#--<#if reportModel.projectModel??>-->
                <#--${reportModel.projectModel.name} --->
            <#--</#if>-->
            <#--${reportModel.reportName}-->
        <#--</title>-->
        <meta charset="utf-8">
        <title>Application Dependency Graph</title>
        <meta name="viewport" content="width=device-width">
        <style>
            @font-face {
                font-family: 'FontAwesome';
                src: url('resources/fonts/fontawesome-webfont.woff') format('woff');
            }
            @font-face {
              font-family: 'PatternFlyIcons-webfont';
              src: url('resources/fonts/PatternFlyIcons-webfont.woff') format('woff');
            }

            * {
                box-sizing: border-box;
            }

            body {
                margin: 0px;
                font-family: sans-serif;
            }

            kubernetes-topology-graph {
                border: 0px solid lightgray;
                position: fixed;
                height: 100%;
                width: 100%;
            }

            .controls {
                position: absolute;
                bottom: 60px;
                left: 60px;
                background-color: lightgray;
                padding: 0 10px;
            }

            .display {
                position: absolute;
                left: 60px;
                right: 60px;
                padding: 20px 0px 10px;
            }

            #selected {
                float: left;
                display: block;
            }

            .legend {
                font-weight: bold;
            }

        </style>
        <link rel="stylesheet" href="resources/css/topology-graph.css"/>
        <script src="resources/js/angular.min.js"></script>
        <script src="resources/js/d3.v3.min.js" type="text/javascript"></script>
        <script src="resources/js/topology-graph.js"></script>
        <script type="text/javascript" src="resources/js/jquery-1.10.1.min.js"></script>
        <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>
        <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
        <link href="resources/css/windup.css" rel="stylesheet" media="screen"/>
        <link href="resources/css/windup.java.css" rel="stylesheet" media="screen"/>
        <link href="resources/css/jquery-ui.min.css" rel="stylesheet" media="screen"/>
        <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    </head>
    <body role="document" class="application-graph" ng-app="appDependencies">
        <!-- Navbar -->
        <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
            <div class="wu-navbar-header navbar-header">
                <#include "include/navheader.ftl">
            </div>

            <#if applicationReportIndexModel??>
                <div class="navbar-collapse collapse navbar-responsive-collapse">
                    <#include "include/navbar.ftl">
                </div><!-- /.nav-collapse -->
            </#if>
        </div>
        <!-- / Navbar -->

        <div class="container-fluid" role="main" style="padding-left: 0px;padding-right: 0px;">

            <kubernetes-topology-graph items="data.items" relations="data.relations" kinds="kinds">
            </kubernetes-topology-graph>

            <div class="display"><label id="selected"></label></div>

            <div class="controls">

                <label class="legend">Applications: </label>
                <kubernetes-topology-icon kind="Ear">
                    <svg class="app-topology">
                        <use xlink:href="#vertex-Ear" x="15" y="15"></use>
                    </svg>
                </kubernetes-topology-icon>
                <label>EARs</label>

                <kubernetes-topology-icon kind="WarApp">
                    <svg class="app-topology">
                        <use xlink:href="#vertex-WarApp" x="15" y="15"></use>
                    </svg>
                </kubernetes-topology-icon>
                <label>WARs</label>
                <br/>
                <label class="legend">Embedded: </label>
                <kubernetes-topology-icon kind="War">
                    <svg class="app-topology">
                        <use xlink:href="#vertex-War" x="15" y="15"></use>
                    </svg>
                </kubernetes-topology-icon>
                <label>WARs</label>

                <kubernetes-topology-icon kind="Jar">
                    <svg class="app-topology">
                        <use xlink:href="#vertex-Jar" x="15" y="15"></use>
                    </svg>
                </kubernetes-topology-icon>
                <label>JARs</label>

 				<#if reportModel.projectModel??>           
	                <kubernetes-topology-icon kind="ExternalJar">
	                    <svg class="app-topology">
	                        <use xlink:href="#vertex-ExternalJar" x="15" y="15"></use>
	                    </svg>
	                </kubernetes-topology-icon>
	                <label>3rd-party JARs&nbsp;&nbsp;&nbsp;&nbsp; &nbsp; </label>
                </#if>
            </div>

            <svg class="app-topology" hidden>
                <defs>
                    <g class="Ear" id="vertex-Ear">
                        <circle r="15" fill="#fff" stroke="#aaa"></circle>
                        <text y="8" x="-0.5" fill="#0B3C5D" font-family="PatternFlyIcons-webfont" font-size="18px" text-anchor="middle">&#xe918;</text>
                    </g>
                    <g class="WarApp" id="vertex-WarApp">
                        <circle r="15" fill="#fff" stroke="#aaa"></circle>
                        <text y="5" x="0.5" fill="#0B3C5D" font-family="FontAwesome" font-size="16px" text-anchor="middle">&#xf1b3;</text>
                    </g>
                    <g class="War" id="vertex-War">
                        <circle r="15" fill="#fff" stroke="#aaa"></circle>
                        <text y="5" x="0.5" fill="#328CC1" font-family="FontAwesome" font-size="16px" text-anchor="middle">&#xf1b3;</text>
                    </g>
                    <g class="Jar" id="vertex-Jar">
                        <circle r="15" fill="#fff" stroke="#aaa"></circle>
                        <text y="5" x="0.5" fill="#328CC1" font-family="FontAwesome" font-size="16px" text-anchor="middle">&#xf1b2;</text>
                    </g>
                    <#if reportModel.projectModel??> 
                    <g class="ExternalJar" id="vertex-ExternalJar">
                        <circle r="15" fill="#fff" stroke="#aaa"></circle>
                        <text y="5" x="0.5" fill="#D9B310" font-family="FontAwesome" font-size="16px" text-anchor="middle">&#xf1b2;</text>
                    </g>
                    </#if>
                </defs>
            </svg>
		<script src="resources/js/app-dependency-graph.js"></script>
		<#if reportModel.projectModel??>
        <script src="data/${reportModel.projectModel.rootFileModel.SHA1Hash}_app_dependencies_graph.js"></script>
        <#else>
        <script src="data/app_dependencies_graph.js"></script>
        </#if>
    </body>
</html>
