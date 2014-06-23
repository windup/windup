<!DOCTYPE html>
<html lang="en">

<#macro tagRenderer tag>
	<span class="label label-${tag.level.name()?lower_case}"><#nested/></span>
</#macro>

<#macro resourceRenderer resource>
	<tr><td>${resource.resourceName}</td>
		<td>
			<#list resource.technologyTags as tag>
		    <@tagRenderer tag>${tag.title}</@tagRenderer>
		    </#list>
		</td>
		<td>
			<#list resource.issueTags as tag>
		    <@tagRenderer tag>${tag.title}</@tagRenderer>
		    </#list>
		</td>
	</tr>
</#macro>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${application.applicationName} - Application Report</title>
    <link href="../../resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="../../resources/css/windup.css" rel="stylesheet" media="screen">
  </head>
  <body role="document">
    
    <!-- Fixed navbar -->
    <div class="navbar-fixed-top windup-bar" role="navigation">
      <div class="container theme-showcase" role="main">
        <img src="../../resources/img/windup-logo.png" class="logo"/>
      </div>
    </div>



    <div class="container" role="main">
        <div class="row">
          <div class="page-header page-header-no-border">
            <h1>Application Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${application.applicationName}</small></h1>
             <div class="navbar navbar-default">
            <div class="navbar-header">
              <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
              </button>
            </div>
            <div class="navbar-collapse collapse navbar-responsive-collapse">
              <ul class="nav navbar-nav">
                <li><a href="index.html">Application</a></li>
                <li><a href="ejbs.html">EJBs</a></li>
                <li><a href="hibernate.html">Hibernate</a></li>
                <li><a href="spring.html">Spring</a></li>
                <li><a href="server-resources.html">Server Resources</a></li>
                <li><a href="classloader-blacklists.html">Blacklists</a></li>
                <li><a href="classloader-duplicate.html">Duplicates</a></li>
                <li><a href="classloader-notfound.html">Not Found</a></li>
              </ul>
            </div><!-- /.nav-collapse -->
            </div>
          </div>
        </div>
    </div>






    <div class="container theme-showcase" role="main">

	<#list application.archives as archive>
	<div class="panel panel-${archive.level.name()?lower_case}">
        <div class="panel-heading">
            <h3 class="panel-title">${archive.applicationPath}</h3>
        </div>
        <table class="table table-striped table-bordered">
          <tr>
            <th>Name</th><th>Technology</th><th>Issues</th>
          </tr>
          <#list archive.resources as resource>
			<@resourceRenderer resource/>
          </#list>
        </table>
    </div>
    </#list>

    </div> <!-- /container -->


    <script src="https://code.jquery.com/jquery.js"></script>
    <script src="../../resources/js/bootstrap.min.js"></script>
  </body>
</html>