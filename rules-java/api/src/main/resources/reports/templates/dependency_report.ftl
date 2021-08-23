<!DOCTYPE html>
<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

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
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
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
        </div><!-- /.nav-collapse -->
    </div>
    <!-- / Navbar -->

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">${reportModel.reportName}
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="${reportModel.description}"></i></div>
                    <#if reportModel.projectModel??>
                        <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                    </#if>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

            <#list sortDependencyGroupArchivesByPathAscending(reportModel.archiveGroups)>
            <div class="dependencies">
                <#items as dependency>
                    <#assign dependencyProject = dependency.canonicalProject>
                    <#assign archiveName = dependencyProject.rootFileModel.fileName>
                    <#if dependencyProject??>
                    <div class="panel panel-default panel-primary dependency">
                        <div id="${archiveName}" class="panel-heading">
                            <h3 class="panel-title">${archiveName}</h3>
                        </div>
                        <div class="panel-body">
                            <dl class="traits dl-horizontal">
                                <#assign gav = dependencyProject.mavenIdentifier!?trim >
                                <#assign sha1 = dependency.SHA1!"">
                                <#if gav?? && gav?trim?has_content>
                                    <dt class="trait">Maven coordinates:</dt>
                                    <dd id="${archiveName}-maven">
                                        <#if sha1?has_content>
                                            <#assign sha1URL = 'http://search.maven.org/#search|ga|1|1:"' + sha1?url('ISO-8859-1') + '"'>
                                            <a id="${archiveName}-gav" href="${sha1URL?html}" target="_blank">${gav}</a>
                                        <#else>
                                            ${gav}
                                        </#if>
                                    </dd>
                                </#if>
                                <#if sha1?trim?has_content>
                                    <dt class="trait">SHA1 hash:</dt>
                                    <dd id="${archiveName}-hash">${sha1}</dd>
                                </#if>
                                <#if dependencyProject.version??>
                                    <dt class="trait">Version:</dt>
                                    <dd id="${archiveName}-version">${dependencyProject.version}</dd>
                                </#if>
                                <#if dependencyProject.organization??>
                                    <dt class="trait">Organization:</dt>
                                    <dd id="${archiveName}-org">${dependencyProject.organization}</dd>
                                </#if>
                                <dt class="trait">Found at path:</dt>
                                <dd>
                                    <ul id="${archiveName}-paths" class="list-unstyled">
                                        <#list sortDependencyArchivesByPathAscending(dependency.archives) as edge>
                                            <li>${edge.fullPath}</li>
                                        </#list>
                                    </ul>
                                </dd>
                            </dl>
                        </div>
                    </div>
                    </#if>
                </#items>
            </div>
            </#list>

        </div> <!-- /container -->
    </div><!--/row-->

    <#include "include/timestamp.ftl">
    </div><!-- /container main-->


    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
