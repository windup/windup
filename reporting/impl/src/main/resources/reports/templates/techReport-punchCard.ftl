<#if reportModel.applicationReportIndexModel??>
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>
        <#if reportModel.projectModel??>
            ${reportModel.projectModel.name} -
        </#if>
        ${reportModel.reportName}
    </title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/css/jquery-ui.min.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
    </style>
</head>
<body role="document">
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

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">${reportModel.reportName}</div>
                    <#if reportModel.projectModel??>
                        <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                    </#if>
                </h1>
                <div class="desc">
                    ${reportModel.description}
                </div>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

                MAIN CONTENT HERE

            </div>
        </div>
    </div>

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery-ui.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script src="resources/js/jquery.tablesorter.min.js"></script>
    <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
    <script src="resources/libraries/handlebars/handlebars.4.0.5.min.js"></script>
</body>
</html>