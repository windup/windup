<!DOCTYPE html>
<html lang="en">
<#assign index_page = true>
<#if reportModel.applicationReportIndexModel??>
    <#assign navUrlPrefix = "reports/">
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>

<#macro tagRenderer tag>
    <#if tag.level?? && tag.level == "IMPORTANT">
        <span class="label label-info label-important" title="${tag.level}">
    <#else>
        <span class="label label-info" title="${tag.level}">
    </#if>
        <#nested/></span>
</#macro>

<#macro applicationReportRenderer appReport>
    <#-- appReport : ApplicationReportModel -->

    <#assign allTraversal  = getProjectTraversal(appReport.projectModel, 'all')>
    <#assign incidentCountByCategory = getEffortCountForProjectByIssueCategory(event, allTraversal, true)>

    <#include "include/effort_util.ftl">
    <#assign allTraversal  = getProjectTraversal(appReport.projectModel, 'all')>
    <#assign pointsFromAllTraversal = getMigrationEffortPointsForProject(allTraversal, true) >

    <#--assign onceTraversal  = getProjectTraversal(appReport.projectModel, 'only_once')>
    <#assign pointsFromOnceTraversal = getMigrationEffortPointsForProject(onceTraversal, true) -->

    <#-- For VIRTUAL apps, or if there is no VIRTUAL app, skip computing of the shared points. -->
    <#assign showSharedPoints = appReport.projectModel.projectType! != "VIRTUAL" && sharedLibsExists>
    <#if showSharedPoints>
        <#assign sharedTraversal = getProjectTraversal(appReport.projectModel, 'shared')>
        <#assign pointsFromSharedTraversal = getMigrationEffortPointsForProject(sharedTraversal, true) >
    <#else>
        <#assign pointsFromSharedTraversal = 0 >
    </#if>

    <#-- Total Effort Points, Name, Technologies, Incident Count per Severity-->
    <div class="appInfo pointsShared${pointsFromSharedTraversal}">
        <div class="stats">
            <div class="effortPoints total">
                <span class="points">${pointsFromAllTraversal}</span>
                <span class="legend">story points</span>
            </div>
            <#-- If there is no Shared Libraries virtual app, don't show the "column". -->
            <#if sharedLibsExists>
                <div class="effortPoints shared">
                    <span class="points">${pointsFromSharedTraversal}</span>
                    <span class="legend">in shared archives</span>
                </div>
                <div class="effortPoints unique">
                    <span class="points">${pointsFromAllTraversal - pointsFromSharedTraversal}</span>
                    <span class="legend">only in this app</span>
                </div>
            </#if>
            <div class="incidentsCount">
                <table>
                    <tr>
                        <td colspan="2">Number of incidents</td>
                    </tr>
                    <#assign totalIncidents = 0 >
                    <#list incidentCountByCategory?keys as issueCategory>
                        <#assign totalIncidents = totalIncidents + incidentCountByCategory?api.get(issueCategory) >
                        <tr>
                            <td class="count">${incidentCountByCategory?api.get(issueCategory)}</td>
                            <td class="label_">${issueCategory.name}</td>
                        </tr>
                    </#list>
                    <tr class="total">
                        <td class="count"> <span>${totalIncidents}</span> </td>
                        <td class="label_"> <span>Total</span> </td>
                    </tr>
                </table>
            </div>
        </div>

        <div class="traits">
            <div class="fileName">
                <a href="reports/${appReport.reportFilename}" style="float: left; margin-right: 5px; margin-top: 6px;">
                    <#-- For virtual apps, use name rather than the file name. -->
                    ${ (appReport.projectModel.projectType! = "VIRTUAL"
                        && appReport.projectModel.name??)?then(
                            appReport.projectModel.name,
                            appReport.projectModel.rootFileModel.applicationName)}
                </a>
            </div>
            <#if appReport.projectModel.csvFilename??>
                <div class="csvReport">
                    <a href="${appReport.projectModel.csvFilename}">(CSV Report)</a>
                </div>
            </#if>
            <div class="techs" style="clear: left;">
                <#list getTechnologyTagsForProjectTraversal(allTraversal) as tag>
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
    <title>${reportModel.reportName}</title>
    <link href="reports/resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="reports/resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="reports/resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link href="reports/resources/img/mta-icon.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
        body.viewAppList .apps  { margin: 0 2ex; }

        body.viewAppList .apps .appInfo {
            border-bottom: 1px solid gray;
            overflow: auto; width: 100%; /* clearing */
            padding: 1ex 0 1ex;
        }
        body.viewAppList .apps .appInfo .stats { float: right; width: 30%; padding: 0.4ex 0; }
        body.viewAppList .apps .appInfo .stats .effortPoints { float: left; width: 160px; padding: 0.3ex 0.2em 0; font-size: 33pt; }
        body.viewAppList .apps .appInfo .stats .effortPoints        span { display: block; margin: auto; text-align: center; }
        body.viewAppList .apps .appInfo .stats .effortPoints        .points { line-height: 1; color: #294593; }
        body.viewAppList .apps .appInfo .stats .effortPoints        .legend { font-size: 7pt; }
        body.viewAppList .apps .appInfo .stats .effortPoints.shared,
        body.viewAppList .apps .appInfo .stats .effortPoints.unique { width: 90px; font-size: 18pt; margin-top: 23px; }
        /* Hide the "cell" if the app has 0 shared points". */
        body.viewAppList .apps .appInfo.pointsShared0 .stats .effortPoints.shared,
        body.viewAppList .apps .appInfo.pointsShared0 .stats .effortPoints.unique { visibility: hidden; }
        /* Hide the whole "column" if there's no virtual app (i.e. no shared-libs app). */
        body.viewAppList.noVirtualApp .apps .appInfo  .stats .effortPoints.shared,
        body.viewAppList.noVirtualApp .apps .appInfo  .stats .effortPoints.unique { display: none; }
        body.viewAppList .apps .appInfo .stats .effortPoints.shared .points,
        body.viewAppList .apps .appInfo .stats .effortPoints.unique .points { color: #8491a8; /* Like normal, but grayed. */ }

        body.viewAppList .apps .appInfo .stats .incidentsCount { float: left; margin:  0 2ex;}
        body.viewAppList .apps .appInfo .stats .incidentsCount table tr.total td { border-top: 1px solid silver; }
        body.viewAppList .apps .appInfo .stats .incidentsCount .count { text-align: right; padding-right: 1ex; min-width: 7.4ex; }
        body.viewAppList .apps .appInfo .traits { margin-left: 0px; width: 70%;}
        body.viewAppList .apps .appInfo .traits .fileName { padding: 0.0ex 0em 0.2ex; font-size: 18pt; /* color: #008cba; (Default BS link color) */ }
        body.viewAppList .apps .appInfo .traits .techs { }

        /* Specifics for virtual apps. */
        body.viewAppList .apps .virtual .appInfo .traits .fileName { color: #477280; }

        body.viewAppList .apps .appInfo:first-of-type { border-top: 1px solid gray; }
    </style>

    <script src="reports/resources/js/jquery-3.3.1.min.js"></script>
    <script type="text/javascript">
         var TARGET_RUNTIME = JSON.parse('${reportModel.reportProperties["target_runtimes"]}');
     </script>
</head>
<body role="document" class="viewAppList" style="margin: auto;">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
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
                    <div class="main">Application List
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists all analyzed applications. Select an individual application to show more details."></i></div>
                </h1>
            </div>
        </div>

        <#include "include/filtering_bar.ftl">

        <!-- Apps -->
        <#assign sharedLibsExists = reportModel.relatedResources["sharedLibsApplicationReport"]!?has_content >

        <section class="apps">
            <#assign virtualAppExists = false>
            <div class="real">
                <fieldset id="runtimeLegend" style="margin-bottom: 10px;">
                    <legend id="runtimeLegendHeader">
                        <a style="float: left; margin-right: 5px; margin-top: 2px" role="button" data-toggle="collapse" href="#runtimeLegendContent" aria-expanded="false" aria-controls="runtimeLegendContent">Runtime labels legend</a>
                        <div style="display: inline;">
                            <span class="label label-success">Supported</span>
                            <span class="label label-warning">Partially supported</span>
                            <span class="label label-danger">Unsuitable</span>
                            <span class="label label-default">Neutral</span>
                        </div>
                    </legend>
                    <div id="runtimeLegendContent" class="collapse">
                        <dl class="dl-horizontal dl-horizontal-small left"></dl>
                    </div>
                    <br>
                </fieldset>

                <#-- See CreateApplicationListReportRuleProvider -->
                <#--
                <#list iterableToList(reportModel.relatedResources.applications)?sort_by(["projectModel","rootFileModel","fileName"]) as applicationReport>
                -->
                <#list sortApplicationsList(iterableToList(reportModel.relatedResources.applications)) as applicationReport>
                    <#if applicationReport.projectModel.projectType! != "VIRTUAL" >
                        <@applicationReportRenderer applicationReport/>
                    <#else>
                        <#assign virtualAppExists = true>
                    </#if>
                </#list>
            </div>
        </section>

        <#if virtualAppExists>
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Shared Libraries
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This section groups all issues found in libraries included in multiple applications."></i></div>
                </h1>
            </div>
        </div>
        <section class="apps">
            <div class="virtual">
                <#list iterableToList(reportModel.relatedResources.applications)?sort_by(["projectModel","name"]) as applicationReport>
                    <#if applicationReport.projectModel.projectType! = "VIRTUAL" >
                        <@applicationReportRenderer applicationReport/>
                    </#if>
                </#list>
            </div>
        </section>
        <#else>
            <script>$("body").addClass("noVirtualApp");</script>
        </#if>

        <div style="width: 100%; text-align: center">
            <a href="reports/windup_ruleproviders.html">Rule providers execution overview</a>
                |
            <a href="reports/windup_freemarkerfunctions.html">FreeMarker methods</a>
        </div>
        <#include "include/timestamp.ftl">

    </div> <!-- /.container-fluid -->

    <script src="reports/resources/js/windup-utils.js"></script>
    <script type="text/javascript">
        $("body.viewAppList .apps .real .appInfo").sortElements(function(a, b){
            return $(a).find(".traits .fileName").first().text().trim().toLowerCase() > $(b).find(".traits .fileName").first().text().trim().toLowerCase() ? 1 : -1;
        });
    </script>
    <script src="reports/resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
