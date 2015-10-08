<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Server Resource Report</title>
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
                    <div class="main">Server Resource Report</div>
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

			<#if reportModel.relatedResources.datasources.list.iterator()?has_content>
					<div class="panel panel-primary">
			            <div class="panel-heading">
			                <h3 class="panel-title">DataSources</h3>
			            </div>
			            <table class="table table-striped table-bordered">
			        	<tr>
			        		<th class="col-md-4">JNDI Location</th>
			        		<th class="col-md-2">Database</th>
			        		<th class="col-md-6">Links</th>
			        	</tr>

			    		<#list reportModel.relatedResources.datasources.list.iterator() as datasource>
			    			<tr>
								<td>${datasource.jndiLocation}</td>
								<td>
									<#if datasource.databaseTypeName??>
										${datasource.databaseTypeName}<#if datasource.databaseTypeVersion??> ${datasource.databaseTypeVersion}</#if>
									</#if>
								</td>
								<td><@render_linkable linkable=datasource layout='horizontal'/></td>
			    			</tr>
					    </#list>
			    		</table>
			    	</div>
			</#if>


			<#if reportModel.relatedResources.jmsDestinations.list.iterator()?has_content>
					<div class="panel panel-primary">
			            <div class="panel-heading">
			                <h3 class="panel-title">JMS Destinations</h3>
			            </div>
			            <table class="table table-striped table-bordered">
			        	<tr>
			        		<th class="col-md-4">JNDI Location</th>
			        		<th class="col-md-2">Destination Type</th>
			        		<th>Links</th>
			        	</tr>
			    		<#list reportModel.relatedResources.jmsDestinations.list.iterator() as jmsDestination>
			    			<tr>
								<td>${jmsDestination.jndiLocation}</td>
								<td><#if jmsDestination.destinationType??>${jmsDestination.destinationType}</#if></td>
								<td><@render_linkable linkable=jmsDestination layout='horizontal'/></td>
			    			</tr>
					    </#list>
			    		</table>
			    	</div>
			</#if>

			<#if reportModel.relatedResources.jmsConnectionFactories.list.iterator()?has_content>
					<div class="panel panel-primary">
			            <div class="panel-heading">
			                <h3 class="panel-title">JMS Connection Factories</h3>
			            </div>
			            <table class="table table-striped table-bordered">
			        	<tr>
			        		<th>JNDI Location</th>
			        		<th>Connection Factory Type</th>
			        		<th>Links</th>
			        	</tr>
			    		<#list reportModel.relatedResources.jmsConnectionFactories.list.iterator() as jmsConnectionFactory>
			    			<tr>
								<td>${jmsConnectionFactory.jndiLocation}</td>
								<td><#if jmsConnectionFactory.connectionFactoryType??>${jmsConnectionFactory.connectionFactoryType}</#if></td>
								<td></td>
			    			</tr>
					    </#list>
			    		</table>
			    	</div>
			</#if>

			<#if reportModel.relatedResources.otherResources.list.iterator()?has_content>
					<div class="panel panel-primary">
			            <div class="panel-heading">
			                <h3 class="panel-title">Other JNDI Entries</h3>
			            </div>
			            <table class="table table-striped table-bordered">
			        	<tr>
			        		<th>JNDI Location</th>
			        	</tr>
			    		<#list reportModel.relatedResources.otherResources.list.iterator() as other>
			    			<tr>
								<td>${other.jndiLocation}</td>
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