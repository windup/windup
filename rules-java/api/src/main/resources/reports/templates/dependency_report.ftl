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
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
        body.reportJar .dependency { padding: 1ex 1em 1ex; margin: 0ex 1em; border-bottom: 1px solid gray; }
        body.reportJar .dependency h4 { font-size: 14pt !important; color: #286ba4; background-color: #f2f2f2 }
        body.reportJar .dependency * { font-size: 12pt !important; }
        body.reportJar .dependency dl.traits dt.trait { display: block; }
        <!-- body.reportJar .dependency .traits .trait { font-size: 11pt !important; }-->
    </style>
</head>
<body role="document" class="reportJar">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
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
                
            <#list sortDependencyGroupArchivesByPathAscending(reportModel.archiveGroups)>
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Dependencies</h3>
                </div>
                <div class="dependencies">
                <#items as dependency>
                    <#assign dependencyProject = dependency.canonicalProject>
                    <#assign archiveName = dependencyProject.rootFileModel.fileName>
                    <#if dependencyProject??>
                    <div id="${archiveName}" class="dependency panel panel-default">
                        <h4>${archiveName}</h4>
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
                            <#if dependencyProject.name?? && dependencyProject.name != archiveName>
                                <dt class="trait">Name:</dt>
                                <dd id="${archiveName}-name">${dependencyProject.name}</dd>
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
                    </#if>
                </#items>
                </div>
            </div>
            </#list>

        </div> <!-- /container -->
    </div><!--/row-->

    <#include "include/timestamp.ftl">
    </div><!-- /container main-->

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>