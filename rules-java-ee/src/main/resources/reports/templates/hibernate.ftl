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
    
    <!-- Fixed navbar -->
    <div class="navbar-fixed-top windup-bar" role="navigation">
      <div class="container theme-showcase" role="main">
        <img src="resources/img/windup-logo.png" class="logo"/>
      </div>
    </div>



    <div class="container" role="main">
      <div class="row">
        <div class="page-header page-header-no-border">
          <h1>EJB Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
          <div class="navbar navbar-default">
            <div class="navbar-header">
              <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
            </div>
          <div class="navbar-collapse collapse navbar-responsive-collapse">
	        <ol class="breadcrumb top-menu">
		      <li><a href="../index.html">All Applications</a></li>
			  <#include "include/breadcrumbs.ftl">
	        </ol> 
          
          </div><!-- /.nav-collapse -->
          <div class="navbar-collapse collapse navbar-responsive-collapse">
            <ul class="nav navbar-nav">
              <#include "include/navbar.ftl">
            </ul>
          </div><!-- /.nav-collapse -->
      </div>
    </div>


    <div class="container theme-showcase" role="main">

    <#if !reportModel.relatedResources.hibernateConfiguration.list.iterator()?has_content>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Hibernate Configurations</h3>
            </div>
            <div class="panel-body">
                No Hibernate configuration files to report!
            </div>
        </div>
    </#if>

    <#list reportModel.relatedResources.hibernateConfiguration.list.iterator() as hibernateConfiguration>
	    <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Hibernate Configuration: ${hibernateConfiguration.prettyPath}</h3>
            </div>
            <div class="panel-body">
                <div class="col-md-6">
                    <table class="table table-striped table-bordered" id="sessionFactoryPropertiesTable">
                        <tr>
                            <th>Session Property</th><th>Value</th>
                        </tr>
		                <#list hibernateConfiguration.hibernateSessionFactories.iterator() as sessionFactory>
                                <#list sessionFactory.sessionFactoryProperties?keys as sessionPropKey>
		                            <tr>
		                                <td>${sessionPropKey}</td>
		                                <td>${sessionFactory.sessionFactoryProperties[sessionPropKey]}</td>
		                            </tr>
		                        </#list>
		                </#list>
		            </table>
       		    </div>
            </div>
        </div>
    </#list>

	<#if !reportModel.relatedResources.hibernateEntities.list.iterator()?has_content>
        <div class="panel panel-primary">
        	<div class="panel-heading">
                <h3 class="panel-title">Hibernate Entities</h3>
            </div>
            <div class="panel-body">
                No Hibernate entity mapping files found to report!
            </div>
        </div>
    </#if>

    <#if reportModel.relatedResources.hibernateEntities.list.iterator()?has_content>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Hibernate Entities</h3>
            </div>
            <div class="panel-body">
                <div class="row">
              	
                <div class="col-md-6">
    		        <table class="table table-striped table-bordered" id="hibernateEntityTable">
		                <tr>
		                    <th>Hibernate Entity</th><th>Table</th>
		                </tr>
		                <#list reportModel.relatedResources.hibernateEntities.list.iterator() as entity>
		          	        <tr>
		          		        <td>
		          			        <#if entity.javaClass??>
								        ${entity.javaClass.qualifiedName}
							        </#if>
						        </td>
		          		        <td>${entity.tableName!""}</td>
		          	        </tr>
		                </#list>
		            </table>
		         </div>
      	    </div>
          </div>
        </div>
    </#if>

        
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>