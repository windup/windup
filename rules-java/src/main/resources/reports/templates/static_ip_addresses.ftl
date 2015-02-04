<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro tagRenderer tag>
    <span class="label label-${tag.level.name()?lower_case}"><#nested/></span>
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

<#macro staticIpFileRenderer reportModel>
    <div class="panel panel-primary">
    	<div class="panel-heading">
            <h3 class="panel-title">Static IP Addresses</h3>
        </div>

        <table class="table table-striped table-bordered" id="staticIPTable">
            <tr>
                <th>File</th>
                <th>Location</th>
				<th>IP Address</th>
            </tr>

            <#list reportModel.relatedResources.staticIPLocations.list.iterator() as staticIpRef>
            <tr>
                <td>
                	<#assign fileName = getPrettyPathForFile(staticIpRef.file)!> 
					<@fileSourceLink staticIpRef.file fileName/>
                </td>
                <td> <#if staticIpRef.lineNumber?has_content>Line Number ${staticIpRef.lineNumber}, </#if><#if staticIpRef.columnNumber?has_content>Column Number ${staticIpRef.columnNumber} </#if> </td>
				<td> <#if staticIpRef.sourceSnippit?has_content> ${staticIpRef.sourceSnippit} </#if> </td>
            </tr>
            </#list>
        </table>
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
            <ul class="nav navbar-nav">
                <#include "include/navbar.ftl">
            </ul>
        </div>
    </div>
    <!-- / Navbar -->
    
    
    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>Static IP Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
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