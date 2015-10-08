<!DOCTYPE html>
<html lang="en">

<#macro tagRenderer tag>
	<#if tag.level?? && tag.level == "IMPORTANT">
		<span class="label label-danger">
	<#else>
		<span class="label label-info">
	</#if>
		<#nested/></span>
</#macro>

<#macro applicationReportRenderer applicationReport>
	<#if applicationReport.displayInApplicationList>
		<tr>
			<td>
				<a href="reports/${applicationReport.reportFilename}">${applicationReport.projectModel.name}</a>
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
      			${getMigrationEffortPoints(applicationReport.projectModel, true)} Story Points
    		</td>
		</tr>
	</#if>
</#macro>

	<head>
		<meta charset="utf-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<title>Overview - Profiled by Windup</title>

		<!-- Bootstrap -->
		<link href="reports/resources/css/bootstrap.min.css" rel="stylesheet"/>
		<link href="reports/resources/css/windup.css" rel="stylesheet" media="screen"/>
	</head>
	<body role="document">


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
                    <div class="main">Overview</div>
                    <div class="path">Profiled by Windup</div>
                </h1>
            </div>
        </div>
    </div>



    <div class="container-fluid theme-showcase" role="main">

        <!-- Table -->
        <table class="table table-striped table-bordered">
			<tr>
				<th>Name</th><th>Technology</th><th>Effort</th>
			</tr>

			<#list applicationReports as applicationReport>
				<@applicationReportRenderer applicationReport/>
			</#list>

        </table>

    	<div style="width: 100%; text-align: center">
			<a href="reports/windup_ruleproviders.html">All Rules</a>
				|
			<a href="reports/windup_freemarkerfunctions.html">Windup FreeMarker Methods</a>
			    |
			<a href="#" id="jiraFeedbackTrigger">Send Feedback</a>
            <script type="text/javascript" src="https://issues.jboss.org/s/f215932e68571747ac58d0f5d554396f-T/en_US-r7luaf/6346/82/1.4.16/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-US&amp;collectorId=8b9e338b"></script>
            <script type="text/javascript">
                window.ATL_JQ_PAGE_PROPS = {
                    "triggerFunction": function(showCollectorDialog) {
                        jQuery("#jiraFeedbackTrigger").click(function(e) {
                            e.preventDefault();
                            showCollectorDialog();
                        });
                    }
                };
            </script>
		</div>

    </div> <!-- /container -->
    <script src="reports/resources/js/jquery-1.10.1.min.js"></script>
    <script src="reports/resources/js/bootstrap.min.js"></script>
  </body>
</html>
