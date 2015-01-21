<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro tagRenderer tag>
    <span class="label label-${tag.level.name()?lower_case}"><#nested/></span>
</#macro>

<#macro ignoredFileRenderer reportModel>
    <div class="panel panel-primary">
        <table class="table table-striped table-bordered">
            <tr>
                <th>File</th>
                <th>Path</th>
		<th>Ignored by regex</th>
            </tr>

            <#list reportModel.ignoredFiles.iterator() as file>
            <tr>
                <td> <#if file.fileName?has_content> ${file.fileName} </#if> </td>
                <td> <#if file.filePath?has_content> ${file.filePath} </#if> </td>
		<td> <#if file.ignoredRegex?has_content> ${file.ignoredRegex} </#if> </td>
            </tr>
            </#list>
        </table>
    </div>

</#macro>


<#macro fileRegexesRenderer reportModel>
    <div class="panel panel-primary">
        <table class="table table-striped table-bordered">
            <tr>
                <th>Regex</th>
		<th>Compilable</th>
            </tr>

            <#list reportModel.fileRegexes.iterator() as regex>
            <tr>
                <td> <#if regex.regex?has_content> ${regex.regex} </#if> </td>
		<td> <#if regex.compilationError?has_content> ${regex.compilationError}
		     <#else> OK
                     </#if> </td>
            </tr>
            </#list>
        </table>
    </div>

</#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Ignored files</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
</head>
<body role="document">

    <!-- Fixed navbar -->
    <div class="navbar-fixed-top windup-bar" role="navigation">
      <div class="container theme-showcase" role="main">
        <img src="resources/img/windup-logo.png" class="logo"/>
      </div>
    </div>

    <div class="container" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>Ignored files from <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
                <div class="navbar navbar-default">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                    </div>
                    <div class="navbar-collapse collapse navbar-responsive-collapse">
                        <ol class="breadcrumb top-menu">
                            <li><a href="../index.html">All Applications</a></li>
                            <#include "include/breadcrumbs.ftl">
                        </ol>

                    </div><!-- /.nav-collapse -->
                    <div class="navbar-collapse collapse navbar-responsive-collapse">
                        <ul class="nav navbar-nav">
                            <#include "include/navbar.ftl">
                        </ul>
                    </div><!-- /.nav-collapse -->
                </div>
            </div>
        </div>

        <div class="container theme-showcase" role="main">
            <@fileRegexesRenderer reportModel />
            <@ignoredFileRenderer reportModel />
        </div>
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>
