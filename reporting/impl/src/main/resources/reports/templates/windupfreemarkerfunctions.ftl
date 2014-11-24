<!DOCTYPE html>
<html lang="en">

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Windup FreeMarker Functions and Directives</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
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
        <h1>
        	Windup FreeMarker Functions and Directives
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
            <li><a href="../index.html">&lt;- All Applications</a></li>
          </ul>
        </div><!-- /.nav-collapse -->
        <div class="navbar-collapse collapse navbar-responsive-collapse">
          <ul class="nav navbar-nav">
          </ul>
        </div><!-- /.nav-collapse -->
      </div>
    </div>
</div>

    <div class="container theme-showcase" role="main">


		<div class="panel panel-primary">
			<div class="panel-heading">
			    Functions
			</div>
			<table class="table table-striped table-bordered">
			  	<tr>
		    		<th>Function Name</th>			    		
		    		<th>Description</th>
	  			</tr>
	  			<#list getAllFreeMarkerMethods() as freeMarkerMethod>
					<tr>
						<td>${freeMarkerMethod.name}</td>
						<td>${freeMarkerMethod.description} (Implemented by: ${freeMarkerMethod.class})</td>
					</tr>
				</#list>
	  		</table>
		</div>

		<div class="panel panel-primary">
			<div class="panel-heading">
			    Directives
			</div>
			<table class="table table-striped table-bordered">
			  	<tr>
		    		<th>Directive Name</th>			    		
		    		<th>Description</th>
	  			</tr>
				<#list getAllFreeMarkerDirectives() as freeMarkerDirective>
					<tr>
						<td>${freeMarkerDirective.class}</td>
						<td>${freeMarkerDirective.name}</td>
						<td>${freeMarkerDirective.description} (Implemented by: ${freeMarkerDirective.class})</td>
					</tr>
				</#list>
	  		</table>
		</div>
       
    </div> <!-- /container -->


    <script src="resources/js/jquery-1.10.1.min.js"></script>
    
    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    
    <script src="resources/js/bootstrap.min.js"></script>
  </body>
</html>