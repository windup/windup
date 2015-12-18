<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>


<#macro tagRenderer tag>
    <span title="${tag.level}" class="label label-${(tag.level! == 'IMPORTANT')?then('danger','info')} tag-${tag.name?replace(' ','')}">
        <#nested/>
    </span>
</#macro>

<#macro reportLineRenderer reportLinesIterable>
<#list reportLinesIterable.iterator()>
    <div class="panel panel-primary">
        <div class="panel-heading">
            <h3 class="panel-title">Application Messages</h3>
        </div>
        <table class="table table-striped table-bordered">
            <#items as reportLine>
            <tr>
                <td>
                    ${reportLine.message}
                    <@render_rule_link renderType="glyph" ruleID=reportLine.ruleID class="rule-link"/>
                </td>
            </tr>
            </#items>
        </table>
    </div>
</#list>
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
            <div style="clear: both;"/>
        </td>

        <#-- Issues -->
        <#assign warnings = sourceReportModel.sourceFileModel.inlineHintCount + sourceReportModel.sourceFileModel.classificationCount>
        <#-- The ~Count are, in fact, Gremlin queries. Don't call more than once. -->
        <td class="warnCount${warnings}">
            <#if warnings == 1>
                <#list sourceReportModel.sourceFileModel.classificationModels.iterator() as classification>
                    ${classification.classification}
                </#list>
                <#list sourceReportModel.sourceFileModel.inlineHints.iterator() as hintLine>
                    ${hintLine.title}
                </#list>
            <#elseif warnings &gt; 1 >
                <div class="warns">Warnings: ${warnings} items</div>
                <ul class='notifications'>
                    <#assign map = {}>
                    <#list sourceReportModel.sourceFileModel.classificationModels.iterator() as classification>
                        <#assign count = (map[classification.classification]!0) + 1>
                        <#assign map += {classification.classification : count}>
                    </#list>
                    <#list sourceReportModel.sourceFileModel.inlineHints.iterator() as hintLine>
                        <#assign count = (map[hintLine.title]!0) + 1>
                        <#assign map += {hintLine.title : count}>
                    </#list>
                    <#list map?keys as key>
                        <#assign count = map[key]>
                        <li class="warning"> ${key?html} <small>${count}&#215;</small></li>
                    </#list>
                </ul>
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
	<#assign panelStoryPoints = getMigrationEffortPoints(projectModel, false, reportModel.includeTags, reportModel.excludeTags)>

    <div class="panel panel-primary projectBox" data-windup-projectguid="${generateGUID()}" data-windup-project-storypoints="${panelStoryPoints}">
        <div class="panel-heading panel-collapsed clickable">
            <span class="pull-left"><i class="glyphicon glyphicon-chevron-up arrowIcon"></i></span>
            <h3 class="panel-title">${projectModel.rootFileModel.prettyPath?html} (${panelStoryPoints} story points)</h3>
        </div>
        <div class="panel-body" style="display:none">
        <div class="container-fluid summaryMargin">

            <!-- Points -->
            <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                <div class="number">${panelStoryPoints}</div>
                <div>Story Points</div>
            </div>

            <!-- Pie chart -->
            <div class='pieChart col-md-6 pull-right windupPieGraph archiveGraphContainer'>
                <div id="project_${projectModel.asVertex().id?c}_pie" class='windupPieGraph'></div>
            </div>

            <!-- Basic info -->
            <div class="basicInfo col-md-6 pull-right">
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
    </div>
    <#list sortProjectsByPathAscending(projectModel.childProjects) as childProject>
        <@projectModelRenderer childProject/>
    </#list>
</#macro>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.rootFileModel.fileName?html} - Application Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <style>
        body.report-Overview .forCatchall { display: none; }
        body.report-Catchall .forOverview { display: none; }
    </style>
</head>
<body role="document" class="java-application report-${reportModel.reportName}">

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
                <h1>
                    <div class="main"
                    onmouseover="$(this).parent().parent().addClass('showDesc')"
                    onmouseout=" $(this).parent().parent().removeClass('showDesc')"
                          >Application Report</div>
                    <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                </h1>
                <div class="desc">
                    <div class="forOverview">
                        This report shows all items found within an application that may need an attention during the migration process.
                        Examples of such issues are use of incompatible APIs, source platform configuration files,
                        proprietary technologies or obsolete versions of libraries.
                    </div>
                    <div class="forCatchall">
                        The Catchall report lists the items found within given application which Windup discovered using
                        so-called "catch-all rules",
                        which usually react to a common incompatible technology trait, such like a typical Java package name.
                        Items listed in this report will most likely need some migration effort.
                        Also, the technologies found by catch-all rules are good candidates for specific Windup rules.
                        <p>
                        See <a href="http://windup.github.io/windup/docs/latest/html/WindupUserGuide.html#Get-Involved"
                           >Get Involved</a> in Windup User Guide to see how to contribute a Windup rule.
                        </p>
                    </div>
                </div>
            </div>
        </div>

        <div class="row container-fluid">
            <div class='container mainGraphContainer'>
                <!--
                <div class='col-md-3 text-right totalSummary'>
                    <div class='totalLoe'>
                      ${getMigrationEffortPoints(reportModel.projectModel, true, reportModel.includeTags, reportModel.excludeTags)}
                    </div>
                    <div class='totalDesc'>Story Points</div>
                </div>
                -->
                <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                    <div class="number">${getMigrationEffortPoints(reportModel.projectModel, true, reportModel.includeTags, reportModel.excludeTags)}</div>
                    <div>Story Points</div>
                </div>
                <div class='col-md-6 pull-right windupPieGraph'>
                    <div id='application_pie' class='windupPieGraph'/>
                </div>
            </div>
        </div>

        <div class="row container-fluid">
            <div class="theme-showcase" role="main">
                <@reportLineRenderer reportModel.applicationReportLines />
                <div>
                    <a id="collapseAll" href="javascript:collapseAll()">Collapse All</a>
                    <a id="expandAll" href="javascript:expandAll()">Expand All</a>
                </div>
                <@projectModelRenderer reportModel.projectModel />
            </div> <!-- /container -->
        </div>


        <script src="resources/js/jquery-1.10.1.min.js"></script>
        <script src="resources/js/jquery.storageapi.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
        <script src="resources/js/windup-overview.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>

        <@render_pie project=reportModel.projectModel recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />


            <#macro projectPieRenderer projectModel>
                <@render_pie project=projectModel recursive=false elementID="project_${projectModel.asVertex().id?c}_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

            <#list projectModel.childProjects.iterator() as childProject>
                <@projectPieRenderer childProject />
            </#list>
        </#macro>

        <@projectPieRenderer reportModel.projectModel />
     </div>
</body>
</html>
