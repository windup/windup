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
        <#assign incidentCountBySeverity = getEffortCountForProjectBySeverity(applicationReport.projectModel, true)>
        <#assign totalIncidents = 0 >

		<tr>
			<td>
				<a href="reports/${applicationReport.reportFilename}">${applicationReport.projectModel.rootFileModel.fileName}</a>
			</td>
			<td>

				<#list getTechnologyTagsForProject(applicationReport.projectModel) as tag>
					<@tagRenderer tag>
						<#if tag.version?has_content> ${tag.name} ${tag.version}
						<#else>
							${tag.name}
						</#if>
		    		</@tagRenderer>
        		</#list>
    		</td>
    		<td>
      			<#list incidentCountBySeverity?keys as severity>
      			    <div class="col-sm-11 text-right">${severity}</div>
      			    <div class="col-sm-1 text-right">${incidentCountBySeverity?api.get(severity)}</div>
      			    <#assign totalIncidents = totalIncidents + incidentCountBySeverity?api.get(severity) >
      			</#list>
                <div class="col-sm-11 text-right">Total</div>
                <div class="col-sm-1">${totalIncidents}</div>
    		</td>
    		<td class="text-right" style="vertical-align:middle">
    		    <#include "include/effort_util.ftl">
                ${getMigrationEffortPointsForProject(applicationReport.projectModel, true)}
    		</td>
		</tr>
</#macro>

	<head>
		<meta charset="utf-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<title>${reportModel.reportName} - Profiled by Windup</title>

		<!-- Bootstrap -->
		<link href="reports/resources/css/bootstrap.min.css" rel="stylesheet"/>
		<link href="reports/resources/css/windup.css" rel="stylesheet" media="screen"/>
        <link href="reports/resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
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
                <div class="windup-bar" role="navigation">
                    <div class="container theme-showcase" role="main">
                        <img src="reports/resources/img/windup-logo.png" class="logo"/>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="page-header">
                    <h1>
                        <div class="main">Application list</div>
                        <div class="path">Profiled by Windup</div>
                    </h1>
                    <div class="desc">
                        The Application List report shows All applications which were analyzed.
                        Click on the individual application to see individual reports or you can follow
                        Global Migration Issues Report.
                    </div>
                </div>
            </div>
        </div>



        <div class="container-fluid theme-showcase" role="main">

            <!-- Table -->
            <table class="table table-striped table-bordered">
               <tr>
                  <th>Name</th>
                  <th>Technology</th>
                  <th class="text-right">Incident Count</th>
                  <th>Total Effort Points</th>
               </tr>

               <#list reportModel.relatedResources.applications.list.iterator() as applicationReport>
                  <@applicationReportRenderer applicationReport/>
               </#list>
            </table>


        <div style="width: 100%; text-align: center">
            <a href="reports/windup_ruleproviders.html">All Rules</a>
                |
            <a href="reports/windup_freemarkerfunctions.html">Windup FreeMarker Methods</a>
                |
            <a href="#" id="jiraFeedbackTriggerBottomLink">Send Feedback</a>
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
