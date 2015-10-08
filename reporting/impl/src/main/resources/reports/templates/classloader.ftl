<!DOCTYPE html>
<html lang="en">

  <head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>${application.applicationName} - Classloader Report</title>
    <link href="../../resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../../resources/css/windup.css" rel="stylesheet" media="screen"/>
  </head>
  <body role="document">

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Classloader Report</div>
                    <div class="path">${application.applicationName?html}</div>
                </h1>

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






    <div class="container-fluid theme-showcase" role="main">

	<#if classloader.classes?has_content>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">${classloader.type}</h3>
        </div>
			<table class="table table-striped table-bordered">
			<tr>
				<th>${classloader.referencedFrom}</th><th>${classloader.referenceType}</th>
			</tr>
			<#list classloader.classes as clz>
			<tr>
			  <td>${clz.clzName}</td>
			  <td>
			  	<#if clz.references?has_content>
			  	<table>
					<#list clz.references as reference>
						<tr>${reference.referenceType}<td></td><td>${reference.clzName}</td></tr>
					</#list>
				</table>
				</#if>
			  </td>
			</tr>
			</#list>

			</table>
	</div>
	</#if>

    </div> <!-- /container -->


    <script src="https://code.jquery.com/jquery.js"></script>
    <script src="../../resources/js/bootstrap.min.js"></script>
  </body>
</html>