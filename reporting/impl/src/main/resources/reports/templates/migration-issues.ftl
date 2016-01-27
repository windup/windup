<!DOCTYPE html>

<#macro migrationIssuesRenderer problemSummary>
    <tr>
        <td>
            <a href="#" class="problem-link">
                ${problemSummary.issueName}
            </a>
            <div class="problem-file-list list-group" style="display: none;">
                <div style="position:relative; width: 100%;">
                    <!-- Internal issues per file table -->
                    <table class="table table-hover table-condensed tablesorter-child tablesorter">
                        <thead>
                            <tr>
                                <th>File</th>
                                <th>Issues Found</th>
                                <th></th>
                            </tr>
                        </thead>
                        <tbody>
                            <#list problemSummary.descriptions as description>
                                <#list problemSummary.getFilesForDescription(description) as fileSummary>
                                    <tr>
                                        <td>
                                            <@render_link model=fileSummary.file class="list-group-item migration-issues-detailed-item"/><#t>
                                        </td>
                                        <td>
                                            <@render_link model=fileSummary.file text="#{fileSummary.occurences}" class="list-group-item migration-issues-detailed-item"/><#t>
                                        </td>
                                        <td>
                                            <#if fileSummary?is_first>
                                                <a href="#" class="show-detailed-hint">Show Hint</a>
                                            </#if>
                                        </td>
                                    </tr>
                                    <#if fileSummary?is_first>
                                        <tr class="hint-detail-display" style="display: none;">
                                            <td colspan="3">
                                                <div>
                                                    ${markdownToHtml(description!"-- No detailed text --")}
                                                </div>
                                            </td>
                                        </tr>
                                    </#if>
                                </#list>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </td>
        <td>${problemSummary.numberFound}</td>
        <td>${problemSummary.effortPerIncident}</td>
        <td>${getEffortDescriptionForPoints(problemSummary.effortPerIncident)}</td>
        <td>${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
    </tr>
</#macro>

<#function getIncidentsFound problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + problemSummary.numberFound>
    </#list>
    <#return result>
</#function>

<#function getTotalPoints problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + (problemSummary.numberFound * problemSummary.effortPerIncident)>
    </#list>
    <#return result>
</#function>

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
                        ${reportModel.description}
                    </div>
                </div>
            </div>

        <div class="row">
        	<div class="container-fluid theme-showcase" role="main">
	            <div class="panel panel-default panel-primary">
	                <div class="panel-heading">
                        <h3 class="panel-title">Analysis Detail</h3>
                    </div>

                    <#assign problemsBySeverity = getProblemSummaries(reportModel.projectModel, reportModel.includeTags, reportModel.excludeTags)>
                    <#if !problemsBySeverity?has_content>
                        <div class="panel-body">
                            <div>
                                No issues were found by the existing rules. If you would like to add custom rules,
                                see the <a href="https://github.com/windup/windup/wiki/Rules-Development-Guide">
                                Rule Development Guide</a>.
                            </div>
                        </div>
                    </#if>

                    <#list problemsBySeverity?keys as severity>
                        <table class="table table-hover table-condensed tablesorter migration-issues-table">
                            <thead>
                                <tr>
                                    <th class="sortable">Issue</th>
                                    <th class="sortable">Incidents Found</th>
                                    <th class="sortable">Story Points per Incident</th>
                                    <th>Level of Effort</th>
                                    <th class="sortable">Total Story Points</th>
                                </tr>
                                <tr>
                                    <td>
                                        <b>${severity}</b>
                                    </td>
                                    <td>${getIncidentsFound(problemsBySeverity[severity])}</td>
                                    <td></td>
                                    <td></td>
                                    <td>${getTotalPoints(problemsBySeverity[severity])}</td>
                                </tr>
                            </thead>
                            <tbody>
                                <#list problemsBySeverity[severity] as problemSummary>
                                    <@migrationIssuesRenderer problemSummary />
                                </#list>
                            </tbody>
                        </table>
                    </#list>
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
        $(".show-detailed-hint").click(function (e) {
            $(this).parent().parent().next(".hint-detail-display").toggle();
            $(this).text() == "Show Hint" ? $(this).text("Hide Hint") : $(this).text("Show Hint");
            e.preventDefault();
        });

        $(".migration-issues-table").tablesorter({
            selectorHeaders: '> thead > tr > th',
            sortList: [[4,1]],
            headers: {
                // 2nd, 3rd, and 5th columns are parsed using thousands parser
                0: {sorter:'a-elements'},
                1: {sorter:'thousands'},
                2: {sorter:'thousands'},
                3: {sorter:false},
                4: {sorter:'thousands'}
            }
        });
    });

   	    function resizeTables()
        {
            var tableArr = document.getElementsByClassName('migration-issues-table');
            var cellWidths = new Array();

            // get widest
            for(i = 0; i < tableArr.length; i++)
            {
                for(j = 0; j < tableArr[i].rows[0].cells.length; j++)
                {
                   var cell = tableArr[i].rows[0].cells[j];

                   if(!cellWidths[j] || cellWidths[j] < cell.clientWidth)
                        cellWidths[j] = cell.clientWidth;
                }
            }

            // set all columns to the widest width found
            for(i = 0; i < tableArr.length; i++)
            {
                for(j = 0; j < tableArr[i].rows[0].cells.length; j++)
                {
                    tableArr[i].rows[0].cells[j].style.width = cellWidths[j]+'px';
                }
            }
        }

        window.onload = resizeTables;


        </script>
    </body>
</html>
