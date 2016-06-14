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
        JAR Dependency Report
    </title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
        body.reportJar .dependency { padding: 1ex 1em 1ex; margin: 0ex 1em; border-bottom: 1px solid gray; }
        body.reportJar .dependency h4 { font-size: 14pt !important; color: #286ba4; }
        body.reportJar .dependency * { font-size: 12pt !important; }
        body.reportJar .dependency ul.traits li.trait { display: block; }
        body.reportJar .dependency .traits .trait span { font-weight: bold; font-size: 11pt !important; }
    </style>
</head>
<body role="document" class="reportJar">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
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
                    <div class="main">JAR Dependency Report</div>
                    <#if reportModel.projectModel??>
                        <div class="path">${reportModel.projectModel.name?html}</div>
                    </#if>
                </h1>
                <div class="desc">
                    This report lists Java dependencies in order to provide useful information for locating
                    outdated or incompatible dependencies.
                </div>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                
            <#list reportModel.archiveGroups.iterator()>
            <div class="panel panel-primary">
                <div class="panel-heading">
                    <h3 class="panel-title">Dependencies</h3>
                </div>
                <div class="dependencies">
                <#items as dependency>
                    <#assign dependencyProject = dependency.canonicalProject>
                    <#assign archiveName = dependencyProject.rootFileModel.fileName>
                    <#if dependencyProject??>
                    <div class="dependency">
                        <h4>${archiveName}</h4>
                        <ul class="traits">
                            <#assign gav = dependencyProject.mavenIdentifier!?trim >
                            <#assign sha1 = dependency.SHA1!"">
                            <#if gav?? && gav?trim?has_content>
                                <li class="trait">
                                    <span>Maven coordinates:</span>
                                        <#if sha1?has_content>
                                            <#assign sha1URL = 'http://search.maven.org/#search|ga|1|1:"' + sha1?url('ISO-8859-1') + '"'>
                                            <a id="${archiveName}-gav" href="${sha1URL?html}" target="_blank">${gav}</a>
                                        <#else>
                                            ${gav}
                                        </#if>
                                </li>
                            </#if>
                            <#if sha1?trim?has_content>
                                <li class="trait"> <span id="${archiveName}-hash>SHA1 hash:</span> ${sha1} </li>
                            </#if>
                            <#if dependencyProject.name?? && dependencyProject.name != archiveName>
                                <li class="trait">
                                    <span id="${archiveName}-name">Name:</span> ${dependencyProject.name}
                                </li>
                            </#if>
                            <#if dependencyProject.version??>
                                <li class="trait">
                                    <span id="${archiveName}-version">Version:</span> ${dependencyProject.version}
                                </li>
                            </#if>
                            <#if dependencyProject.organization??>
                                <li class="trait">
                                    <span id="${archiveName}-org">Organization:</span> ${dependencyProject.organization}
                                </li>
                            </#if>
                            <li class="trait">
                                <span>Found at paths:</span>
                                <ul id="${archiveName}-paths">
                                    <#list dependency.archives.iterator() as edge>
                                        <div>
                                            ${edge.fullPath?html}
                                        </div>
                                    </#list>
                                </ul>
                            </li>
                        </ul>
                    </div>
                    </#if>
                </#items>
                </div>
            </div>
            </#list>

        </div> <!-- /container -->
    </div><!--/row-->

    </div><!-- /container main-->

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>