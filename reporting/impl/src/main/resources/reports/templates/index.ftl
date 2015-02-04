<!DOCTYPE html>
<html lang="en">

<#macro tagRenderer tag>
	<span class="label label-${tag.level.name()?lower_case}"><#nested/></span> 
</#macro>
 
<#macro applicationReportRenderer applicationReport>
  <#if applicationReport.displayInApplicationList>
	<tr>
	   <td><a href="reports/${applicationReport.reportFilename}">${applicationReport.projectModel.name}</a></td>
	       <td>
      <#-- <#list application.technologyTags as tag>
        <@tagRenderer tag>${tag.title}</@tagRenderer>
        </#list>
      -->
    </td>
    <td>
      <#-- ${application.effort.name()?capitalize} -->
    </td>
    <td>
      <#--
      <#list application.issueTags as tag>
        <@tagRenderer tag>${tag.title}</@tagRenderer>
        </#list>
      -->
   </td>
	</tr>
	</#if>
</#macro>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Overview - Profiled by Windup</title>

    <!-- Bootstrap -->
      <link href="reports/resources/css/bootstrap.min.css" rel="stylesheet">
      <link href="reports/resources/css/windup.css" rel="stylesheet" media="screen">
      
      <style>

      </style>
  </head>
  <body role="document">

    
    <div class="container-fluid" role="main">
    
    	<div class="row">
			<div class="windup-bar" role="navigation">
		      <div class="container theme-showcase" role="main">
		        <img src="reports/resources/img/windup-logo.png" class="logo"/>
		      </div>
		    </div>
		</div>
    	
        <div class="row">
            <div class="page-header">
                <h1>Overview <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">Profiled by Windup</small></h1>
            </div>
        </div>
    </div>



    <div class="container-fluid theme-showcase" role="main">

        <!-- Table -->
        <table class="table table-striped table-bordered">
          <tr>
            <th>Name</th><th>Technology</th><th>Effort</th><th>Issues</th>
          </tr>
          
          <#list applicationReports as applicationReport>
			       <@applicationReportRenderer applicationReport/>
          </#list>
          
        </table>
        
    	<div style="width: 100%; text-align: center">
			<a href="reports/windup_ruleproviders.html">All Rules</a>
				|
			<a href="reports/windup_freemarkerfunctions.html">Windup FreeMarker Methods</a>
		<ul>
	</div>
    </div> <!-- /container -->


    <script src="reports/resources/js/jquery-1.10.1.min.js"></script>
    <script src="reports/resources/js/bootstrap.min.js"></script>
  </body>
</html>
