<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>


<#macro unparsableFilesRenderer subProject>
    <#list (subProject.unparsableFiles)!>
    <div class="panel panel-default panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">${subProject.rootFileModel.prettyPath?html}</h3>
        </div>
        <table class="table unparsableFiles">
            <#items as file>
            <#assign sourceReportModel = fileModelToSourceReport(file)!>
            <tr>
                <td>
                    <div>
                        <#if sourceReportModel.reportFilename?? >
                            <a href="${sourceReportModel.reportFilename}?project=${reportModel.projectModel.getElement().id()?c}">
                                <strong>${file.fileName!}</strong>
                                <span>${file.filePath!}</span>
                            </a>
                        <#else>
                            <strong>${file.fileName!}</strong>
                            <span>${file.filePath!}</span>
                        </#if>
                    </div>
                    <#if file.expectedFormat?has_content>
                    <div><strong>Expected format:</strong> ${file.expectedFormat!}</div>
                    </#if>
                    <#if file.parseError?has_content && file.onParseError! != "IGNORE">
                    <div class="parseError well well-sm">${file.parseError!}</div>
                    </#if>
                </td>
            </tr>
            </#items>
        </table>
    </div>
    <#else>
        <#if subProject.rootFileModel.parseError?has_content && subProject.rootFileModel.onParseError! != "IGNORE">
        <div class="panel panel-default panel-primary">
            <div class="panel-heading error">
                <h3 class="panel-title">${subProject.rootFileModel.prettyPath?html}</h3>
            </div>
            <div style="font-size: 12px; padding: 1ex 1em;">${subProject.rootFileModel.parseError?html}.</div>
        </div>
        <#else>
            <!-- No unparsable files in project ${subProject.rootFileModel.prettyPath?html} -->
        </#if>
    </#list>
</#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Unparsable files</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/img/mta-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
</head>
<body role="document" class="report report-Unparsable">

	<!-- Navbar -->
	<div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <#include "include/navbar.ftl">
        </div><!-- /.nav-collapse -->
    </div>
    <!-- / Navbar -->

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Unparsable Files Report
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="${reportModel.description}"></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <#list reportModel.allSubProjects as subProject>
            <div class="row unparsableFile">
                <div class="container-fluid theme-showcase" role="main">
                    <@unparsableFilesRenderer subProject />
                </div>
            </div>
        <#else>
            <div class="row unparsableNone">
                <h3>Everything OK - Windup didn't have problems parsing any file.</h3>
            </div>
        </#list>

        <#include "include/timestamp.ftl">
    </div> <!-- /container -->


    <script src="resources/js/jquery-3.3.1.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
