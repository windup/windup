
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
	
	<div class="panel panel-primary">
		<div class="panel-heading">
		    <h3 class="panel-title">Spring Beans</h3>
		</div>
		<div class="panel-body">
		    <#if !reportModel.relatedResources.springBeans.list.iterator()?has_content>
		        No Spring Beans Found!
		    </#if>
			<#if reportModel.relatedResources.springBeans.list.iterator()?has_content>
			    <table class="table table-striped table-bordered" id="springBeansTable">
			        <tr>
			            <th>Bean Name</th><th>Java Class</th>
			        </tr>
			        <#list reportModel.relatedResources.springBeans.list.iterator() as springBean>
			            <tr>
                           <td>${springBean.springBeanName!""}</td>
                           <td>
                               <#if springBean.javaClass??>
                                   ${springBean.javaClass.qualifiedName}
                               </#if>
                           </td>
			            </tr>
			        </#list>
			    </table>
			</#if>
		</div><!--end of panel-body-->
	</div>

        
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>