<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${application.applicationName} - Hibernate Report</title>
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
            <h1>Hibernate Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${application.applicationName}</small></h1>
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

    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">Hibernate Session - ABC</h3>
        </div>
        <div class="panel-body">
          <div class="well well-sm" style="height: 300px;">Graph of Hibernate Entities</div>

          <div class="row">
            <div class="col-md-6">
		        <table class="table table-striped table-bordered">
		          <tr>
		            <th>Session Property</th><th>Value</th>
		          </tr>
		          <#list hibernate.sessionProperties as property>
		          <tr>
		            <td>${property.property}</td>
		            <td>${property.value}</td>
		          </tr>
		          </#list>
		        </table>
       		 </div>
        	<div class="col-md-6">
		        <table class="table table-striped table-bordered">
		          <tr>
		            <th>Hibernate Entity</th><th>Table</th>
		          </tr>
		          <#list hibernate.hibernateEntities as entity>
		          	<tr>
		          		<td>${entity.qualifiedName}</td>
		          		<td>${entity.tableName}</td>
		          	</tr>
		          </#list>
		        </table>
		     </div>
      	</div>
      </div>
    </div>

        
    </div> <!-- /container -->


    <script src="https://code.jquery.com/jquery.js"></script>
    <script src="../../resources/js/bootstrap.min.js"></script>
  </body>
</html>