<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>


<#macro mdbRenderer mdb>
	<tr>
	  <td>${mdb.beanName!""}</td>
	  <td>${mdb.ejbClass.qualifiedName!""}</td>
	  <td>${mdb.destination!""}</td>
	</tr>
</#macro>

<#macro ejbRenderer ejb>
	<tr>
	  <td>${ejb.beanName!""}</td>
	  <td>
	      <#if ejb.ejbClass??>
              ${ejb.ejbClass.qualifiedName!""}
          </#if>
      </td>
	  <td>${ejb.sessionType!""}</td>
	</tr>
</#macro>

<#macro entityRenderer ejb>
	<tr>
	  <td>${ejb.beanName!""}</td>
	  <td>
	      <#if ejb.ejbClass??>
              ${ejb.ejbClass.qualifiedName!""}
          </#if>
      </td>
	  <td>${ejb.tableName!""}</td>
	  <td>${ejb.persistenceType!""}</td>
	</tr>
</#macro>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - EJB Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
  </head>
  <body role="document">


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
		</div><!-- /.nav-collapse -->
	</div>

	<div class="container-fluid" role="main">
		<div class="row">
			<div class="page-header page-header-no-border">
				<h1>EJB Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
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

		    <#if !reportModel.relatedResources.mdb.list.iterator()?has_content && !reportModel.relatedResources.stateless.list.iterator()?has_content && !reportModel.relatedResources.stateful.list.iterator()?has_content && !reportModel.relatedResources.entity.list.iterator()?has_content>
				<div class="panel panel-primary">
				    <div class="panel-heading">
		                <h3 class="panel-title">EJB Report</h3>
		            </div>
		            <div class="panel-body">
		                <h3 class="panel-title">No EJBs found to report</h3>
		            </div>
		        </div>
			</#if>

			<#if reportModel.relatedResources.mdb.list.iterator()?has_content>
			    <div class="panel panel-primary">
			        <div class="panel-heading">
			            <h3 class="panel-title">Message Driven Beans</h3>
			        </div>
					<table class="table table-striped table-bordered" id="mdbTable">
						<tr>
							<th>MDB Name</th><th>Class</th><th>Queue</th>
						</tr>
						<#list reportModel.relatedResources.mdb.list.iterator() as mdb>
							<@mdbRenderer mdb />
						</#list>
					</table>
				</div>
			</#if>

			<#if reportModel.relatedResources.stateless.list.iterator()?has_content>
			    <div class="panel panel-primary">
			        <div class="panel-heading">
			            <h3 class="panel-title">Stateless Session Beans</h3>
			        </div>
					<table class="table table-striped table-bordered" id="statelessTable">
						<tr>
							<th>Bean Name</th><th>Class</th><th>Type</th>
						</tr>
						<#list reportModel.relatedResources.stateless.list.iterator() as statelessBean>
							<@ejbRenderer statelessBean/>
						</#list>
					</table>
			    </div>
		    </#if>

		    <#if reportModel.relatedResources.stateful.list.iterator()?has_content>
			    <div class="panel panel-primary">
			        <div class="panel-heading">
			            <h3 class="panel-title">Stateful Session Beans</h3>
			        </div>
			        <table class="table table-striped table-bordered" id="statefulTable">
			            <tr>
			              <th>Bean Name</th><th>Class</th><th>Type</th>
			            </tr>
						<#list reportModel.relatedResources.stateful.list.iterator() as statefulBean>
			            	<@ejbRenderer statefulBean/>
			            </#list>
			        </table>
			    </div>
		    </#if>

			<#if reportModel.relatedResources.entity.list.iterator()?has_content>
				<div class="panel panel-primary">
					<div class="panel-heading">
						<h3 class="panel-title">Entity Beans</h3>
					</div>
					<table class="table table-striped table-bordered" id="entityTable">
						<tr>
						<th>Bean Name</th><th>Class</th><th>Table</th><th>Persistence Type</th>
						</tr>
						<#list reportModel.relatedResources.entity.list.iterator() as entityBean>
							<@entityRenderer entityBean/>
						</#list>
					</table>
				</div>
			</#if>
    	</div> <!-- /container -->
	</div> <!-- /row-->

	</div><!-- /container main-->
    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>
