<!DOCTYPE html>

<#macro migrationIssuesRenderer problemSummary>
    <tr rospan="2">
        <td>
            <a href="#" class="toggle">
                ${problemSummary.issueName}
            </a>
        </td>
        <td class="text-right">${problemSummary.numberFound}</td>
        <td class="text-right">${problemSummary.effortPerIncident}</td>
        <td>${getEffortDescriptionForPoints(problemSummary.effortPerIncident, true)}</td>
        <td class="text-right">${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
    </tr>
    <tr class="tablesorter-childRow bg-info">
        <td><div class="indent"><strong>File</strong></td>
        <td><strong>Incidents Found</strong></td>
        <td colspan="3"><strong>Hint</strong></td>
    </tr>
    <#list problemSummary.descriptions as description>
        <#assign filesCount=problemSummary.getFilesForDescription(description)?size>
        <#list problemSummary.getFilesForDescription(description) as fileSummary>
            <tr class="tablesorter-childRow">
                <td>
                    <div class="indent">
                        <@render_link model=fileSummary.file class="migration-issues-detailed-item"/><#t>
                    </div>
                </td>
                <td class="text-right">
                    <@render_link model=fileSummary.file text="#{fileSummary.occurences}" class="migration-issues-detailed-item"/><#t>
                </td>
                <#if fileSummary?is_first>
                    <td colspan="3" rowspan="${filesCount}">
                        <div class="panel panel-default hint-detail-panel">
                            <div class="panel-heading">
                                <h4 class="panel-title pull-left">Issue Detail: ${problemSummary.issueName}</h4>
                                <#if problemSummary.ruleID??>
                                <div class="pull-right"><a class="sh_url" title="${problemSummary.ruleID}" href="windup_ruleproviders.html#${problemSummary.ruleID}">Show Rule</a></div>
                                </#if>
                                <div class="clearfix"></div>
                            </div>
                            <div class="panel-body">
                                ${markdownToHtml(description!"-- No detailed text --")}
                            </div>
                            <#list problemSummary.links!>
                            <div class="panel-body">
                                Related resources:
                                <ul>
                                <#items as link>
                                    <li><a href="${link.link}">${link.title}</a></li>
                                </#items>
                                </ul>
                            </div>
                            </#list>
                        </div>
                    </td>
                </#if>
            </tr>
        </#list>
    </#list>

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
        <link href="resources/css/jquery-ui.css" rel="stylesheet" media="screen">
        <link href="resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
        <style>.hint-detail-panel .panel-body { padding-bottom: 0; }</style>
    </head>
    <body role="document">
        <!-- Navbar -->
        <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
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
                        <div class="main">${reportModel.reportName} Report</div>
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

                    <div class="panel-body">
                    <#list problemsBySeverity?keys as severity>
                        <table class="table table-hover table-bordered table-condensed tablesorter migration-issues-table">
                            <thead>
                                <tr>
                                    <th class="sortable">Issue by Category</th>
                                    <th class="sortable">Incidents Found</th>
                                    <th class="sortable">Story Points per Incident</th>
                                    <th>Level of Effort</th>
                                    <th class="sortable">Total Story Points</th>
                                </tr>
                                <tr style="background: silver;">
                                    <td>
                                        <b>${severity}</b>
                                    </td>
                                    <td class="text-right">${getIncidentsFound(problemsBySeverity[severity])}</td>
                                    <td></td>
                                    <td></td>
                                    <td class="text-right">${getTotalPoints(problemsBySeverity[severity])}</td>
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
        </div>

        <script src="resources/js/jquery-1.10.1.min.js"></script>
        <script src="resources/js/jquery-ui.min.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>
        <script src="resources/js/jquery.tablesorter.min.js"></script>
        <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {

                var $table = $('.tablesorter');

                // hide child rows & make draggable
                $table.find('.tablesorter-childRow')
                    .find('td')
                    .droppable({
                        accept: '.draggingSiblings',
                        drop: function(event, ui) {
                            if ($(this).closest('tr').length){
                                $(this).closest('tr').before(
                                    ui.draggable
                                        .css({ left: 0, top: 0 })
                                        .parent()
                                        .removeClass('draggingRow')
                                    );
                                $table
                                    .find('.draggingSiblingsRow')
                                    .removeClass('draggingSiblingsRow')
                                    .find('.draggingSiblings')
                                    .removeClass('draggingSiblings');
                                $table.trigger('update');
                            } else {
                                return false;
                            }
                        }
                    })
                    .draggable({
                        revert: "invalid",
                        start: function( event, ui ) {
                            $(this)
                                .parent()
                                .addClass('draggingRow')
                                .prevUntil('.tablesorter-hasChildRow')
                                .nextUntil('tr:not(.tablesorter-childRow)')
                                .addClass('draggingSiblingsRow')
                                .find('td')
                                .addClass('draggingSiblings');
                        }
                    })
                    .hide();

                $table
                    .tablesorter({
                        // this is the default setting
                        cssChildRow: "tablesorter-childRow"
                    })
                    .delegate('.toggle', 'click' ,function(){
                        $(this)
                            .closest('tr')
                            .nextUntil('tr.tablesorter-hasChildRow')
                            .find('td').toggle();
                        return false;
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
