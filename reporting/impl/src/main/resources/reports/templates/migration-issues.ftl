<!DOCTYPE html>
<html lang="en">
    <#if reportModel.applicationReportIndexModel??>
        <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
    </#if>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>
            <#if reportModel.projectModel??>
                ${reportModel.projectModel.name} -
            </#if>
            ${reportModel.reportName} Report
        </title>
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

            <#if applicationReportIndexModel??>
                <div class="navbar-collapse collapse navbar-responsive-collapse">
                    <#include "include/navbar.ftl">
                </div><!-- /.nav-collapse -->
            </#if>
        </div>
        <!-- / Navbar -->

        <div class="container-fluid" role="main">
            <div class="row">
                <div class="page-header page-header-no-border">
                    <h1>
                        <div class="main"
                        onmouseover="$(this).parent().parent().addClass('showDesc')"
                        onmouseout=" $(this).parent().parent().removeClass('showDesc')"
                        >${reportModel.reportName} Report</div>
                        <#if reportModel.projectModel??>
                            <div class="path">${reportModel.projectModel.name?html}</div>
                        </#if>
                    </h1>
                    <div class="desc">
                        The ${reportModel.reportName} report is a numerical summary of all issues found.
                        Click on the individual issue types to see where it was found.
                    </div>
                </div>
            </div>

        <div class="row">
        	<div class="container-fluid theme-showcase" role="main">
	            <div class="panel panel-default panel-primary">
	            	<div class="panel-heading">
                        <h3 class="panel-title">Potential Issues</h3>
                    </div>
	                <table id="issues_table" class="table table-hover table-condensed tablesorter">
	                    <thead>
	                        <tr>
	                            <th class="sortable">Issue</th>
	                            <th class="sortable">Incidents Found</th>
	                            <th class="sortable">Story Points per Incident</th>
	                            <th class="sortable">Total Story Points</th>
	                            <th class="col-md-1">Rule</th>
	                        </tr>
	                    </thead>
	                    <tbody>
	                        <#list getProblemSummaries(reportModel.projectModel) as problemSummary>
	                            <tr>
	                                <td>
	                                    <a href="#" class="problem-link">
	                                        ${problemSummary.issueName}
	                                    </a>
	                                    <div class="problem-file-list list-group" style="display: none;">
                                        <!-- Internal issues per file table -->
                                        <table id="issues_per_file_table" class="table table-hover table-condensed tablesorter-child tablesorter">
                                            <thead>
                                                <tr>
                                                    <th>File</th>
                                                    <th>Issues Found</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <#list problemSummary.files as fileSummary>
                                                    <tr>
                                                        <td>
                                                            <@render_link model=fileSummary.file class="list-group-item migration-issues-detailed-item"/><#t>
                                                        </td>
                                                        <td>
                                                            <@render_link model=fileSummary.file text="#{fileSummary.occurences}" class="list-group-item migration-issues-detailed-item"/><#t>
                                                        </td>
                                                    </tr>
                                                </#list>
                                            </tbody>
                                        </table>
	                                </td>
			                        <td>${problemSummary.numberFound}</td>
			                        <td>${problemSummary.effortPerIncident}</td>
			                        <td>${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
			                        <td>
			                            <@render_rule_link ruleID=problemSummary.ruleID renderType="glyph" />
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
        <script src="resources/js/jquery.tablesorter.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                $('.problem-link').each(function(index, value) {
                    $(value).click(function(e) {
                        e.preventDefault();
                        $(value).siblings(".problem-file-list").toggle();
                    });
                });
            });
            // we need these parsers because we are using comma to separate thousands and are also sorting links
            $.tablesorter.addParser({
   		 id: 'thousands',
   		 is: function(s) {
  		      return true;
   		 },
 		 format: function(s) {
   		     return s.replace('$','').replace(/,/g,'');
   		 },
  		 type: 'numeric'
	    });
        $.tablesorter.addParser({
        id: 'a-elements',
        is: function(s)
        {
            // return false so this parser is not auto detected 
            return false;
        },
        format: function(s)
        {
            // format your data for normalization 
            return s.replace(new RegExp(/<.*?>/),"");
        },
        type: 'text'
    }); 
            $(document).ready(function() {
                $("#issues_table").tablesorter({
                selectorHeaders: '> thead > tr > th',
                sortList: [[3,1]],
        	headers: {
                  // 2nd,3rd,4th columns are parsed using thousands parser
                  0: {sorter:'a-elements'},
          	      1: {sorter:'thousands'},
                  2: {sorter:'thousands'},
                  3: {sorter:'thousands'},
                  4: {sorter: false}
                  }
                });
   	    });
        </script>
    </body>
</html>
