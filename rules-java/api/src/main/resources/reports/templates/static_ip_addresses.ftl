<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro staticIpFileRenderer reportModel>
    <div class="panel panel-primary">
    	<div class="panel-heading">
            <h3 class="panel-title">Static IP Addresses</h3>
        </div>

		<#if iterableHasContent(reportModel.relatedResources.staticIPLocations)>
	        <table class="table table-striped table-bordered" id="staticIPTable">
	            <tr>
	                <th>File</th>
					<th>Location</th>
					<th>IP Address</th>
	            </tr>

	            <#list reportModel.relatedResources.staticIPLocations.list.iterator() as staticIpRef>
	            <tr>
	                <td>
						<@render_link model=staticIpRef />
	                </td>
	                <td> <#if staticIpRef.lineNumber?has_content>Line: ${staticIpRef.lineNumber}, </#if><#if staticIpRef.columnNumber?has_content>Position: ${staticIpRef.columnNumber} </#if> </td>
					<td> <#if staticIpRef.sourceSnippit?has_content> ${staticIpRef.sourceSnippit} </#if> </td>
	            </tr>
	            </#list>
	        </table>
        <#else>
	        <div class="panel-body">
		        No Static IP Addresses.
		    </div>
        </#if>
    </div>
</#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Static IP Address Files</title>
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
        </div>
    </div>
    <!-- / Navbar -->


    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Static IP Report</div>
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
	            <@staticIpFileRenderer reportModel />
	        </div>
	    </div>
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>