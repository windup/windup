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
</head>
<body role="document">

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

            <#list reportModel.relatedResources["dependencies"].list.iterator()>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Dependencies</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="dependencyTable">
                        <#items as dependency>
                            <#assign dependencyProject = dependency.projectModel>
                            <#if dependencyProject??>
                            <tr>
                                <td>
                                    <strong>${dependency.archiveName}</strong>
                                    <ul class="list-group">
                                        <#assign gav = dependencyProject.asVertex().getProperty('mavenIdentifier')!?trim >
                                        <#if gav?? && gav?trim?has_content >
                                            <li class="trait">
                                                <span>Maven coordinates:</span> ${gav}
                                            </li>
                                        </#if>
                                        <#if dependencyProject.name?? && dependencyProject.name != dependency.archiveName>
                                            <li class="list-group-item">
                                                Name: ${dependencyProject.name}
                                            </li>
                                        </#if>
                                        <#if dependencyProject.version??>
                                            <li class="list-group-item">
                                                Version: ${dependencyProject.version}
                                            </li>
                                        </#if>
                                        <#if dependencyProject.organization??>
                                            <li class="list-group-item">
                                                Organization: ${dependencyProject.organization}
                                            </li>
                                        </#if>
                                        <li class="list-group-item">
                                            File Paths:
                                            <ul>
                                                <#list getArchivesBySHA1(dependency.SHA1Hash).iterator() as instance>
                                                    <li>${instance.prettyPath}</li>
                                                </#list>
                                            </ul>
                                        </li>
                                    </ul>
                                </td>
                            </tr>
                            </#if>
                        </#items>
                    </table>
                </div>
            </#list>

        </div> <!-- /container -->
    </div><!--/row-->

    </div><!-- /container main-->

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>