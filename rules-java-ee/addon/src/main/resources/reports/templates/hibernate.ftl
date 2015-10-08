<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Hibernate Report</title>
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
                    <div class="main">Hibernate Report</div>
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

		    <#list reportModel.relatedResources.hibernateConfiguration.list.iterator() as hibernateConfiguration>
		    	<#list hibernateConfiguration.hibernateSessionFactories.iterator() as sessionFactory>
			    	<#if iterableHasContent(sessionFactory.sessionFactoryProperties?keys)>
					    <div class="panel panel-primary">
				            <div class="panel-heading">
				                <h3 class="panel-title">Session Factory: ${hibernateConfiguration.prettyPath}</h3>
				            </div>
		                    <table class="table table-striped table-bordered" id="sessionFactoryPropertiesTable">
		                        <tr>
		                            <th>Session Property</th><th>Value</th>
		                        </tr>
		                        <#list sessionFactory.sessionFactoryProperties?keys as sessionPropKey>
		                            <tr>
		                                <td>${sessionPropKey}</td>
		                                <td>${sessionFactory.sessionFactoryProperties[sessionPropKey]}</td>
		                            </tr>
		                        </#list>
				            </table>
				        </div>
			        </#if>
		        </#list>
		    </#list>

		    <#if reportModel.relatedResources.hibernateEntities.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">Entities</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="hibernateEntityTable">
		                <tr>
		                    <th>Hibernate Entity</th><th>Table</th>
		                </tr>
		                <#list reportModel.relatedResources.hibernateEntities.list.iterator() as entity>
		          	        <tr>
		          		        <td>
		          		        	<@render_link model=entity.javaClass/>
						        </td>
		          		        <td>${entity.tableName!""}</td>
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