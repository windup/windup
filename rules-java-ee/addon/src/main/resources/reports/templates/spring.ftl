<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Spring Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
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
                    <div class="main">Spring Bean Report</div>
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
				<div class="panel panel-primary">
					<div class="panel-heading">
					    <h3 class="panel-title">Spring Beans</h3>
					</div>

				    <#if !iterableHasContent(reportModel.relatedResources.springBeans)>
				    <div class="panel-body">
				        No Spring Beans Found!
				    </div>
				    </#if>
					<#if iterableHasContent(reportModel.relatedResources.springBeans)>
					    <table class="table table-striped table-bordered" id="springBeansTable">
					        <tr>
					            <th>Bean Name</th><th>Java Class</th>
					        </tr>
					        <#list reportModel.relatedResources.springBeans.list.iterator() as springBean>
					            <tr>
		                           <td><@render_link model=springBean.springConfiguration text=springBean.springBeanName/></td>
		                           <td><@render_link model=springBean.javaClass /></td>
					            </tr>
					        </#list>
					    </table>
					</#if>
				</div><!--end of panel-->
    		</div> <!-- /container -->
		</div><!-- /row -->
	</div><!-- /container main -->
    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>