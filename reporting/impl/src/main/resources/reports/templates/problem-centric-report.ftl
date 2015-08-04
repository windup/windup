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
                <#include "include/navbar.ftl">
            </div><!-- /.nav-collapse -->
        </div>
        <!-- / Navbar -->

        <div class="container-fluid" role="main">
            <div class="row">
                <div class="page-header page-header-no-border">
                    <h1>Migration Issue Report <span class="slash">/</span><small style="margin-left: 20px; font-weight: 100;">${reportModel.projectModel.name}</small></h1>
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
	            <div class="panel panel-default panel-primary">
	            	<div class="panel-heading">
                        <h3 class="panel-title">Potential Issues</h3>
                    </div>
	                <table class="table table-hover table-condensed">
	                    <thead>
	                        <tr>
	                            <th></th>
	                            <th>Issue</th>
	                            <th>Incidents Found</th>
	                            <th>Effort per Incident</th>
	                            <th>Total Effort Points</th>
	                            <th class="col-md-1">Rule</th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#list getProblemSummaries() as problemSummary>
	                            <tr>
	                                <th class="row">${problemSummary?counter}</th>
	                                <td>
	                                    <a href="#" class="problem-link">
	                                        ${problemSummary.issueName}
	                                    </a>
	                                    <div class="problem-file-list list-group" style="display: none;">
	                                        <#list problemSummary.files as file>
	                                            <@render_link model=file class="list-group-item"/><#t>
	                                        </#list>
	                                    </div>
	                                </td>
			                        <td>${problemSummary.numberFound}</td>
			                        <td>${problemSummary.effortPerIncident}</td>
			                        <td>${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
			                        <td>
			                            <@render_rule_link ruleID=problemSummary.ruleID renderType="glyph"/>
			                        </td>
	                            </tr>
	                        </#list>
	                    </tbody>
	                </table>
	            </div>
	    	</div>
        </div>

        <script src="resources/js/jquery-1.10.1.min.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                $('.problem-link').each(function(index, value) {
                    $(value).click(function(e) {
                        e.preventDefault();
                        $(value).siblings(".problem-file-list").toggle();
                    });
                });
            });
        </script>
    </body>
</html>
