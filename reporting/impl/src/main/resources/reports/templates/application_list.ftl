<!DOCTYPE html>
<html lang="en">
<#assign index_page = true>
<#if reportModel.applicationReportIndexModel??>
    <#assign navUrlPrefix = "reports/">
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>

<#macro tagRenderer tag>
	<#if tag.level?? && tag.level == "IMPORTANT">
		<span class="label label-danger" title="${tag.level}">
	<#else>
		<span class="label label-info" title="${tag.level}">
	</#if>
		<#nested/></span>
</#macro>

<#macro applicationReportRenderer applicationReport>
        <#assign allTraversal = getProjectTraversal(applicationReport.projectModel, 'all')>

        <#assign incidentCountBySeverity = getEffortCountForProjectBySeverity(allTraversal, true)>
        <#assign totalIncidents = 0 >

        <#-- Total Effort Points, Name, Technologies, Incident Count per Severity-->
		<div class="appInfo">
            <div class="stats">
                <div class="effortPoints">
                    <#include "include/effort_util.ftl">
                    <span class="points">${getMigrationEffortPointsForProject(allTraversal, true)}</span>
                    <span class="legend">story points</span>
                </div>
                <div class="incidentsCount">
                    <table>
                        <#list incidentCountBySeverity?keys as severity>
                            <#assign totalIncidents = totalIncidents + incidentCountBySeverity?api.get(severity) >
                            <tr>
                                <td class="label_"> ${severity} </td>
                                <td class="count"> ${incidentCountBySeverity?api.get(severity)}&times; </td>
                            </tr>
                        </#list>
                        <tr class="total">
                            <td class="label_"> <span>Total</span> </td>
                            <td class="count"> <span>${totalIncidents}&times;</span> </td>
                        </tr>
                    </table>
                </div>
            </div>

            <div class="traits">
                <div class="fileName">
                    <a href="reports/${applicationReport.reportFilename}">${applicationReport.projectModel.rootFileModel.fileName}</a>
                </div>
                <div class="techs">
                    <#list getTechnologyTagsForProject(applicationReport.projectModel) as tag>
                        <#if tag.name != "Decompiled Java File">
                        <@tagRenderer tag>
                            ${tag.name} <#if tag.version?has_content>${tag.version}</#if>
                        </@tagRenderer>
                        </#if>
                    </#list>
                </div>
            </div>
		</div>
</#macro>

	<head>
		<meta charset="utf-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<title>${reportModel.reportName} - Profiled by Windup</title>

		<!-- Bootstrap -->
		<link href="reports/resources/css/bootstrap.min.css" rel="stylesheet"/>
		<link href="reports/resources/css/windup.css" rel="stylesheet" media="screen"/>
        <link href="reports/resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
        <style>
            body.viewAppList .apps  { margin: 0 2ex; }
            body.viewAppList .apps .appInfo {
                border-bottom: 1px solid gray;
                overflow: auto; width: 100%; /* clearing */
                margin: 1ex 0;
                padding: 1ex 0 2ex;
            }
            body.viewAppList .apps .appInfo .stats { float: left; width: 330px; padding: 0.4ex 0; }
            body.viewAppList .apps .appInfo .stats .effortPoints { float: left; width: 160px; padding: 0.3ex 0.2em 0; font-size: 33pt; }
            body.viewAppList .apps .appInfo .stats .effortPoints span { display: block; margin: auto; text-align: center; }
            body.viewAppList .apps .appInfo .stats .effortPoints .points { line-height: 1; color: rgb(41, 69, 147); }
            body.viewAppList .apps .appInfo .stats .effortPoints .legend { font-size: 7pt; }
            body.viewAppList .apps .appInfo .stats .incidentsCount { float: left; margin:  0 2ex;}
            body.viewAppList .apps .appInfo .stats .incidentsCount table tr.total td { border-top: 1px solid silver; }
            body.viewAppList .apps .appInfo .stats .incidentsCount .count { text-align: right; padding-left: 10px; }
            body.viewAppList .apps .appInfo .traits { margin-left: 340px; }
            body.viewAppList .apps .appInfo .traits .fileName { padding: 0.0ex 0em 0.2ex; font-size: 18pt; }
            body.viewAppList .apps .appInfo .traits .techs { }
        </style>
	</head>
	<body role="document" class="viewAppList">

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
                <div class="windup-bar" role="navigation">
                    <div class="container theme-showcase" role="main">
                        <img src="reports/resources/img/windup-logo.png" class="logo"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="page-header">
                    <h1>
                        <div class="main">Application List</div>
                        <div class="path">Profiled by Windup</div>
                    </h1>
                    <div class="desc">
                        The Application List report shows all applications which were analyzed.
                        Click on an individual application to see individual reports or you can follow
                        the global Migration Issues report.
                    </div>
                </div>
            </div>
        </div>



        <div class="container-fluid theme-showcase" role="main">

            <!-- Table -->
            <div class="apps">
                <#list reportModel.relatedResources.applications.list.iterator() as applicationReport>
                    <@applicationReportRenderer applicationReport/>
                </#list>
            </div>


        <div style="width: 100%; text-align: center">
            <a href="reports/windup_ruleproviders.html">Executed rules overview</a>
                |
            <a href="reports/windup_freemarkerfunctions.html">Windup FreeMarker methods</a>
                |
            <a href="#" id="jiraFeedbackTriggerBottomLink">Send feedback</a>
            <script type="text/javascript" src="https://issues.jboss.org/s/f215932e68571747ac58d0f5d554396f-T/en_US-r7luaf/6346/82/1.4.16/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&amp;collectorId=8b9e338b"></script>
            <script type="text/javascript">
                var existingTriggerFunction = window.ATL_JQ_PAGE_PROPS.triggerFunction;
                window.ATL_JQ_PAGE_PROPS = {
                    "triggerFunction": function(showCollectorDialog) {
                        jQuery("#jiraFeedbackTriggerBottomLink").click(function(e) {
                            e.preventDefault();
                            showCollectorDialog();
                        });
                        if (existingTriggerFunction)
                            existingTriggerFunction(showCollectorDialog);
                    }
                };
            </script>
        </div>

        </div> <!-- /container -->
        <script src="reports/resources/js/jquery-1.10.1.min.js"></script>
        <script src="reports/resources/js/bootstrap.min.js"></script>
    </body>
</html>
