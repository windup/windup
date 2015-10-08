<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro mdbRenderer mdb>
	<tr>
    	<td><@render_link model=mdb.ejbDeploymentDescriptor text=mdb.beanName /></td>
    	<td>
    		<@render_link model=mdb.ejbClass/>
    	</td>
    	<td><#if mdb.destination??>${mdb.destination.jndiLocation}</#if></td>
	</tr>
</#macro>


<#macro ejbRenderer ejb>
    <tr>
        <td>
            <@render_link model=ejb.ejbDeploymentDescriptor text=ejb.beanName/>
        </td>
        <td style="text-align:center">
        	<#if ejb.ejbLocal??>
    			<@render_link model=ejb.ejbLocal text="Local" class="btn btn-xxs btn-success"/><#t>
    		<#else>
    			<a style="visibility:hidden" class="btn btn-xxs btn-default disabled">Local</a><#t>
    		</#if>
    		<#if ejb.ejbRemote??>
            	<@render_link model=ejb.ejbRemote text="Remote" class="btn btn-xxs btn-danger"/><#t>
    		<#else>
    			<a style="visibility:hidden" class="btn btn-xxs btn-default disabled">Remote</a><#t>
            </#if>
        </td>
        <td>
            <@render_link model=ejb.ejbClass/>
        </td>
        <td>
        	<#if ejb.jndiReference??>${ejb.jndiReference.jndiLocation}</#if>
        </td>
    </tr>
</#macro>

<#macro entityRenderer ejb>
    <tr>
        <td>
            <@render_link model=ejb.ejbDeploymentDescriptor text=ejb.beanName />
        </td>
        <td>
            <@render_link model=ejb.ejbClass/>
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
			<#include "include/navbar.ftl">
		</div><!-- /.nav-collapse -->
	</div>

	<div class="container-fluid" role="main">
		<div class="row">
			<div class="page-header page-header-no-border">
                <h1>
                    <div class="main">EJB Report</div>
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
							<th class="col-md-2">MDB Name</th><th>Class</th><th class="col-md-3">JMS Destination</th>
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
							<th class="col-md-2">Bean Name</th><th style="width:130px">Interface</th><th>Implementation</th><th class="col-md-3">JNDI Location</th>
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
			              <th class="col-md-2">Bean Name</th><th style="width:130px">Interface</th><th>Implementation</th><th class="col-md-3">JNDI Location</th>
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
						<th class="col-md-2">Bean Name</th><th>Class</th><th class="col-md-2">Table</th><th class="col-md-1">Persistence Type</th>
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
