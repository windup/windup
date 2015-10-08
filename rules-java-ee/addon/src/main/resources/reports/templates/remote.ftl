<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Remote Service Report</title>
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
                    <div class="main">Remote Service Report</div>
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

		    <#if reportModel.relatedResources.jaxRsServices.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">JAX-RS Services (REST)</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th class="col-md-6">Service Path</th><th class="col-md-6">Implementation</th>
		                </tr>
		                <#list reportModel.relatedResources.jaxRsServices.list.iterator() as service>
		          	        <tr>
		          	        	<td>${service.path}</td>
		          		        <td>
		          		        	<@render_link model=service.implementationClass/>
						        </td>
		          	        </tr>
		                </#list>
		            </table>
		        </div>
		    </#if>

		    <#if reportModel.relatedResources.jaxWsServices.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">JAX-WS Services (SOAP)</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
		                </tr>
		                <#list reportModel.relatedResources.jaxWsServices.list.iterator() as service>
		          	        <tr>
		          	        	<td>
		          	        		<@render_link model=service.interface/>
								</td>
		          		        <td>
		          		        	<@render_link model=service.implementationClass/>
						        </td>
		          	        </tr>
		                </#list>
		            </table>
		        </div>
		    </#if>

		    <#if reportModel.relatedResources.ejbRemoteServices.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">Remote EJB Services</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
		                </tr>
		                <#list reportModel.relatedResources.ejbRemoteServices.list.iterator() as service>
		          	        <tr>
		          	        	<td>
		          	        		<@render_link model=service.interface/>
								</td>
		          		        <td>
		          		        	<@render_link model=service.implementationClass/>
						        </td>
		          	        </tr>
		                </#list>
		            </table>
		        </div>
		    </#if>

			<#if reportModel.relatedResources.rmiServices.list.iterator()?has_content>
		        <div class="panel panel-primary">
		            <div class="panel-heading">
		                <h3 class="panel-title">RMI Services</h3>
		            </div>
    		        <table class="table table-striped table-bordered" id="jpaEntityTable">
		                <tr>
		                    <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
		                </tr>
		                <#list reportModel.relatedResources.rmiServices.list.iterator() as service>
		          	        <tr>
		          	        	<td>
		          	        		<@render_link model=service.interface/>
								</td>
		          		        <td>
		          		        	<@render_link model=service.implementationClass/>
						        </td>
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