<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro tagRenderer tag>
	<#if tag.level?? && tag.level == "IMPORTANT">
		<span class="label label-danger">
	<#else>
		<span class="label label-info">
	</#if>
		<#nested/></span>
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

	<!-- Navbar -->
	<div class="navbar navbar-default navbar-fixed-top">
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
                    <div class="main">Ignored Files Report</div>
                    <div class="path">${reportModel.projectModel.name?html}</div>
                </h1>
            </div>
        </div>

        <div class="row">
        <!-- Breadcrumbs -->
	        <div class="container-fluid">
	            <ol class="breadcrumb top-menu">
	                <li><a href="../index.html">All Applications</a></li>
	                <#include "include/breadcrumbs.ftl">
	            </ol>
	        </div>
        <!-- / Breadcrumbs -->
		</div>

		<div class="row">
	        <div class="container-fluid theme-showcase" role="main">
	            <@fileRegexesRenderer reportModel />
	            <@ignoredFileRenderer reportModel />
	        </div>
        </div>
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>
