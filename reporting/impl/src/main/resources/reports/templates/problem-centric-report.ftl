<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Migration Issues</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
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
			<ul class="nav navbar-nav">
			<#include "include/navbar.ftl">
			</ul>
		</div><!-- /.nav-collapse -->
	</div>
	<!-- / Navbar -->

    <div class="container-fluid" role="main">
		<div class="row">
			<div class="page-header page-header-no-border">
				<h1>Application Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
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
		  Proprietary IBM JMS Class
		</div>
		<div class="row">
          Number of incidents
        </div>

	    <script src="resources/js/jquery-1.10.1.min.js"></script>
	    <script src="resources/js/bootstrap.min.js"></script>
	 </div>
  </body>
</html>
