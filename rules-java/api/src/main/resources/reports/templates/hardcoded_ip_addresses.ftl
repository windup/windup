<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro hardcodedIpFileRenderer reportModel>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">Hard-coded IP Addresses</h3>
        </div>

        <#if iterableHasContent(reportModel.relatedResources.hardcodedIPLocations)>
            <table class="table table-striped table-bordered" id="hardcodedIPTable">
                <tr>
                    <th>File</th>
                    <th>Location</th>
                    <th>IP Address</th>
                </tr>

                <#list reportModel.relatedResources.hardcodedIPLocations as hardcodedIpRef>
                <tr>
                    <td>
                        <@render_link model=hardcodedIpRef project=reportModel.projectModel/>
                    </td>
                    <td> <#if hardcodedIpRef.lineNumber?has_content>Line: ${hardcodedIpRef.lineNumber}, </#if><#if hardcodedIpRef.columnNumber?has_content>Position: ${hardcodedIpRef.columnNumber} </#if> </td>
                    <td> <#if hardcodedIpRef.sourceSnippit?has_content> ${hardcodedIpRef.sourceSnippit} </#if> </td>
                </tr>
                </#list>
            </table>
        <#else>
            <div class="panel-body">
                No Hard-coded IP Addresses.
            </div>
        </#if>
    </div>
    </#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Hard-coded IP Address Files</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/img/tackle-icon.png" rel="shortcut icon" type="image/x-icon"/>

    <script src="resources/js/jquery-3.3.1.min.js"></script>
</head>
<body role="document">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <#include "include/navbar.ftl">
        </div>
    </div>
    <!-- / Navbar -->


    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Hard-coded IP Addresses
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="The hard-coded IP report lists the places where an IP address is found in the application. This may include places where the IP address is specified directly in the code rather than externalized into the configuration files."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                <@hardcodedIpFileRenderer reportModel />
            </div>
        </div>

        <#include "include/timestamp.ftl">
    </div> <!-- /container -->



    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
