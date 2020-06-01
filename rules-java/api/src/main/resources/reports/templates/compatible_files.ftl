<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
<#assign allTraversal = getProjectTraversal(reportModel.projectModel, 'all')>

<#macro tagRenderer tag>
    <#if tag.level?? && tag.level == "IMPORTANT">
        <span class="label label-danger">
    <#else>
        <span class="label label-info">
    </#if>
        <#nested/></span>
</#macro>



<#macro fileSourceLink fileRef name>
    <#if fileRef??>
        <#assign sourceReportModel = fileModelToSourceReport(fileRef)!>
        <#if sourceReportModel.reportFilename??>
            <a href="${sourceReportModel.reportFilename}"> ${name!""} </a>
        <#else>
            ${name!""}
        </#if>
    <#else>
        ${name!""}
    </#if>

</#macro>


<#macro fileModelRenderer fileModel>
    <#if fileModel.prettyPathWithinProject?has_content>
    <tr>
        <td>
            <#assign fileName = getPrettyPathForFileWithExtensions(fileModel)!>
            <@fileSourceLink fileModel fileName/>
        </td>
        <td>
            <#-- <#list resource.technologyTags as tag>
            <@tagRenderer tag>${tag.title}</@tagRenderer>
            </#list> -->
        </td>
    </tr>
    </#if>
</#macro>

<#macro projectModelRenderer traversal>
    <#assign projectModel = traversal.canonicalProject>
    <#assign fileModelCollection = sortFilesByPathAscending(findFilesNotClassifiedOrHinted(projectModel.fileModelsNoDirectories))>
    <#if iterableHasContent(fileModelCollection)>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">${projectModel.rootFileModel.prettyPath}</h3>
            </div>
            <table class="table table-striped table-bordered">
                <tr>
                    <th class="col-md-8">Name</th><th class="col-md-4">Technology</th>
                </tr>
                <#list fileModelCollection as fileModel>
                    <@fileModelRenderer fileModel/>
                </#list>
            </table>
        </div>
    </#if>
    <#list sortProjectTraversalsByPathAscending(traversal.children) as childTraversal>
        <@projectModelRenderer childTraversal/>
    </#list>
</#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Application Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/img/mta-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
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
                    <div class="main">Compatible Files Report
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="Files in this report are believed to be compatible with the selected target platform. However, it is possible that this report contains incompatible files that were not identified by any ${getWindupBrandName()} rules. It is recommended that these files be reviewed manually for any issues."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid">
                <div class="alert alert-info">
                  <p><strong>DISCLAIMER:</strong> Files in this report are believed to be compatible with the selected target platform;
                  however, it is possible that this report contains incompatible files that were not identified by any rules in the system. It is
                  recommended that these files be reviewed manually for any issues.</p>
                </div>
            </div>
        </div>


        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                <@projectModelRenderer allTraversal />
            </div> <!-- /container -->
        </div>

        <#include "include/timestamp.ftl">
    </div>

    <script src="resources/js/jquery-3.3.1.min.js"></script>

    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>

    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
