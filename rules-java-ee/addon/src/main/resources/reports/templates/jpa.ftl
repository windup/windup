<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - JPA Report</title>
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
		</div><!-- /.nav-collapse -->
	</div>
	<!-- / Navbar -->

	<div class="container-fluid" role="main">
		<div class="row">
			<div class="page-header page-header-no-border">
                <h1>
                    <div class="main">JPA Report</div>
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

		    <#list reportModel.relatedResources.jpaConfiguration.list.iterator() as jpaConfiguration>
			    <#list jpaConfiguration.persistenceUnits.iterator() as persistenceUnit>
			    <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">Persistence Unit: ${persistenceUnit.name}</h3>
		            </div>
		            <div class="panel-body">
						<dl class="dl-horizontal small">
							<dt>JPA Configuration</dt>
							<dd>${jpaConfiguration.prettyPath}</dd>

							<#if jpaConfiguration.specificationVersion??>
								<dt>JPA Version</dt>
								<dd>${jpaConfiguration.specificationVersion}</dd>
							</#if>
						</dl>

						<#if persistenceUnit.properties?has_content>
			                <table class="table table-striped table-bordered" id="persistenceUnitPropertiesTable">
		                        <tr>
		                            <th class="col-md-6">Persistence Unit Property</th><th class="col-md-6">Value</th>
		                        </tr>
		                        <#list persistenceUnit.properties?keys as propKey>
		                            <tr>
		                                <td class="col-md-6">${propKey}</td>
		                                <td class="col-md-6">${persistenceUnit.properties[propKey]}</td>
		                            </tr>
		                        </#list>
				            </table>
			            </#if>

						<#if iterableHasContent(persistenceUnit.dataSources)>
							<table class="table table-striped table-bordered">
		                        <tr>
		                            <th class="col-md-6">Data Source</th><th class="col-md-6">Type</th>
		                        </tr>
							<#list persistenceUnit.dataSources.iterator() as dataSource>
								<tr>
									<td class="col-md-6">${dataSource.jndiLocation!""}</td>
									<td class="col-md-6">${dataSource.databaseTypeName!""}</td>
								</tr>
							</#list>
							</table>
						</#if>
		            </div>
		        </div>
		        </#list>
		    </#list>

		    <#if reportModel.relatedResources.jpaEntities.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">JPA Entities</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th>Entity Name</th><th>JPA Entity</th><th>Table</th>
		                </tr>
		                <#list reportModel.relatedResources.jpaEntities.list.iterator() as entity>
		          	        <tr>
		          	        	<td>${entity.entityName}</td>
		          		        <td>
		          		        	<@render_link model=entity.javaClass/>
						        </td>
		          		        <td>${entity.tableName!""}</td>
		          	        </tr>
		                </#list>
		            </table>
		        </div>
		    </#if>

		    <#if reportModel.relatedResources.jpaNamedQueries.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">JPA Named Queries</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th>Query Name</th>
		                    <th>Query</th>
		                </tr>
		                <#list reportModel.relatedResources.jpaNamedQueries.list.iterator() as named>
		          	        <tr>
		          	        	<td>${named.queryName}</td>
		          		        <td>${named.query}</td>
		          	        </tr>
		                </#list>
		            </table>
		        </div>
		    </#if>
	    </div> <!-- /container -->
	</div><!--/row-->

	</div><!-- /container main-->

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>
