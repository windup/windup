<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>


<#macro tagRenderer tag>
	<#if tag.level?? && tag.level == "IMPORTANT">
		<span class="label label-danger">
	<#else>
		<span class="label label-info">
	</#if>
            <#nested/>
        </span>
</#macro>

<#macro reportLineRenderer reportLinesIterable>
<#if reportLinesIterable.iterator()?has_content>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">Application Messages</h3>
        </div>
        <table class="table table-striped table-bordered">

            <#list reportLinesIterable.iterator() as reportLine>
            <tr>
                <td>
                    ${reportLine.message}
                    <@render_rule_link ruleID=reportLine.ruleID/>
                </td>
            </tr>
            </#list>
        </table>
    </div>
</#if>
</#macro>

<#macro fileModelRenderer fileModel>
    <#if !isReportableFile(fileModel, reportModel.includeTags, reportModel.excludeTags) >
        <#return>
    </#if>

    <#assign sourceReportModel = fileModelToSourceReport(fileModel)!>
    <#if sourceReportModel.reportFilename?? >

	<tr>
        <#-- Name -->
        <td>
            <a href="${sourceReportModel.reportFilename}">
                ${getPrettyPathForFile(fileModel)}
            </a>
        </td>
        <#-- Technology -->
		<td>
			<#list getTechnologyTagsForFile(fileModel).iterator() as tag>
                <@tagRenderer tag>
                    ${tag.name} ${tag.version!}
                </@tagRenderer>
            </#list>
			<#list getTagsFromFileClassificationsAndHints(fileModel) as tag>
        		<span class="label label-info tag">${tag}</span>
            </#list>
		</td>
        <#-- Issues -->
		<td>
        <#if sourceReportModel.sourceFileModel.inlineHints.iterator()?has_content || sourceReportModel.sourceFileModel.classificationModels.iterator()?has_content>
            <#assign warnings = sourceReportModel.sourceFileModel.inlineHintCount + sourceReportModel.sourceFileModel.classificationCount>
            <!-- TODO: Move the different rendering to CSS. -->
            <#if warnings gt 1>
                <b>Warnings: ${warnings} items</b>
                <ul class='notifications'>
            </#if>
            <#list sourceReportModel.sourceFileModel.classificationModels.iterator()>
                <#items as classification>
                    <#if warnings == 1 >
                        ${classification.classification}
                    <#else>
                        <li class='warning clsf'>${classification.classification?html}</li>
                    </#if>
                </#items>
            </#list>
            <#list sourceReportModel.sourceFileModel.inlineHints.iterator()>
                <#items as hintLine>
                    <#if warnings == 1 >
                        ${hintLine.title?html}
                    <#else>
                        <li class='warning hint'>${hintLine.title?html}</li>
                    </#if>
                </#items>
            </#list>
            <#if warnings gt 1></ul></#if>
        </#if>
		</td>
        <#-- Story points -->
		<td>
            <#assign fileEffort = getMigrationEffortPointsForFile(sourceReportModel.sourceFileModel)>
            ${fileEffort}
		</td>
	</tr>
	</#if>
</#macro>

<#macro projectModelRenderer projectModel>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">${projectModel.rootFileModel.prettyPath?html}</h3>
        </div>
        <div class="container-fluid summaryMargin">
            <div class='col-md-3 text-right totalSummary'>
                <div class='totalLoe'>
                    ${getMigrationEffortPoints(projectModel, false, reportModel.includeTags, reportModel.excludeTags)}
                </div>
                <div class='totalDesc'>Story Points</div>
            </div>

            <div class='col-md-6 pull-right windupPieGraph archiveGraphContainer'>
                <div id="project_${projectModel.asVertex().getId()?string("0")}_pie" class='windupPieGraph'></div>
            </div>

			<div class="col-md-6 pull-right">
				<table class="table">
					<tr>
						<th>Organization</th>
						<th>Version</th>
						<th>Link</th>
					</tr>
					<tr>
						<td>
						<#assign organizations = projectModelToOrganizations(projectModel)>

						<#if iterableHasContent(organizations)>
							<#list organizations.iterator() as organization>
								${organization.name?html}
							</#list>
						</#if>
						</td>

						<td>${projectModel.name!""?html}</td>
						<td>
							<#if projectModel.url?has_content>
								<a href="${projectModel.url?html}">Project Site</a>
							</#if>

							<#if projectModelSha1Archive(projectModel)?has_content>
								<#assign sha1URL = '|ga|1|1:"' + projectModelSha1Archive(projectModel) + '"'>
								<#assign sha1URL = 'http://search.maven.org/#search' + sha1URL?url('ISO-8859-1')>
								<a href="${sha1URL?html}">Maven Central</a>
							</#if>
						</td>
					</tr>
					<tr>
						<th>Description</th>
					</tr>
					<tr>
						<td colspan="3">
							${projectModel.description!""}
						</td>
					</tr>
				</table>
			</div>

        </div>
        <#if iterableHasContent(projectModel.fileModelsNoDirectories)>
        <table class="table table-striped table-bordered">
          <tr>
            <th class="col-md-6">Name</th><th class="col-md-1">Technology</th><th>Issues</th><th class="col-md-1">Story Points</th>
          </tr>
          <#list sortFilesByPathAscending(projectModel.fileModelsNoDirectories) as fileModel>
             <@fileModelRenderer fileModel/>
          </#list>
        </table>
        </#if>
    </div>
  <#list sortProjectsByPathAscending(projectModel.childProjects) as childProject>
    <@projectModelRenderer childProject/>
  </#list>
</#macro>

    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <title>${reportModel.projectModel.name?html} - Application Report</title>
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

            <div class="row container-fluid">
                <div class='container mainGraphContainer'>
                    <div class='col-md-3 text-right totalSummary'>
                        <div class='totalLoe'>
                          ${getMigrationEffortPoints(reportModel.projectModel, true, reportModel.includeTags, reportModel.excludeTags)}
                        </div>
                        <div class='totalDesc'>Story Points</div>
                    </div>
                    <div class='col-md-6 pull-right windupPieGraph'>
                        <div id='application_pie' class='windupPieGraph'/>
                    </div>
                </div>
            </div>

            <div class="row container-fluid">
                <div class="theme-showcase" role="main">
                    <@reportLineRenderer reportModel.applicationReportLines />
                    <@projectModelRenderer reportModel.projectModel />
                </div> <!-- /container -->
            </div>


            <script src="resources/js/jquery-1.10.1.min.js"></script>

            <script src="resources/libraries/flot/jquery.flot.min.js"></script>
            <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>

            <script src="resources/js/bootstrap.min.js"></script>

            <@render_pie project=reportModel.projectModel recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />


            <#macro projectPieRenderer projectModel>
                <@render_pie project=projectModel recursive=false elementID="project_${projectModel.asVertex().getId()?string(\"0\")}_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

                <#list projectModel.childProjects.iterator() as childProject>
                    <@projectPieRenderer childProject />
                </#list>
            </#macro>

            <@projectPieRenderer reportModel.projectModel />
         </div>
    </body>
</html>
