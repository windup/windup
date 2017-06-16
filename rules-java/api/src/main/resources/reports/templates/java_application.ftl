<!DOCTYPE html>
<html lang="en">

<#include "include/effort_util.ftl">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
<#assign allTraversal = getProjectTraversal(reportModel.projectModel, 'all')>

<#macro tagRenderer tag class="">
    <span title="${tag.level}" class="label label-${(tag.level! == 'IMPORTANT')?then('danger','info')} tag-${tag.name?replace(' ','')} ${class!}"
          data-windup-tag="${tag.name?html}">
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
                <td class="application-message">
                    ${reportLine.message}
                    <@render_rule_link renderType="glyph" ruleID=reportLine.ruleID class="rule-link"/>
                </td>
            </tr>
            </#items>
        </table>
    </div>
</#list>
</#macro>

<#-- Renderer for the table row of given file in an archive (subproject) -->
<#macro fileModelRenderer fileModel>
    <#if !isReportableFile(fileModel, reportModel.includeTags, reportModel.excludeTags) >
        <#return>
    </#if>

    <#assign sourceReportModel = fileModelToSourceReport(fileModel)!>
    <#if sourceReportModel.reportFilename?? >
    <tr class="projectFile">
        <#-- Name -->
        <td class="path">
            <a href="${sourceReportModel.reportFilename}?project=${reportModel.projectModel.asVertex().id?c}">
                ${getPrettyPathForFile(fileModel)}
            </a>
        </td>
        <#-- Technology -->
        <td class="tech">
            <#list getTechnologyTagsForFile(fileModel).iterator() as tag>
                <@tagRenderer tag>
                    ${tag.name} ${tag.version!}
                </@tagRenderer>
            </#list>
            <#list getTagsFromFileClassificationsAndHints(fileModel) as tag>
                <span class="label label-info tag" data-windup-tag="${tag?html}">${tag}</span>
            </#list>
            <div style="clear: both;"></div>
        </td>

        <#-- Incidents -->
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
                <ul class="notifications">
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

<#-- Note that it is possible for projectModel and canonicalProject to be the same. However, if a duplicate project is involved,
 then "projectModel" will be the duplicate and "canonicalProject" will the the canonical instance. -->
<#macro renderAppTreeData projectModel canonicalProject>
    <script>
        <#assign projectID = "project_${projectModel.asVertex().id?c}">
        <#assign canonicalProjectID = "project_${canonicalProject.asVertex().id?c}">

        thisProject = new ProjectNode("${projectModel.rootFileModel.fileName?js_string}", "${projectID}-${canonicalProjectID}");
        thisProject.sourceBased = ${projectModel.sourceBased!false?c};
        var $tagLabels = $("#${projectID} .projectFile .tech .label");
        var $tagWarns = $tagLabels.find(".warning");
        $tagWarns.add( $tagLabels.find(".danger") );
        thisProject.warnings = $tagWarns.size();
        thisProject.tags = []; /// TODO: Need to execute all this after the tag JS analysis.

        if (parentProject == null) {
            rootProject = thisProject;
            console.log("Root project set to: " + rootProject.name + ", id: " + rootProject.id);
        } else {
            parentProject.addSubproject(thisProject);
            console.log("Adding child: " + thisProject.name + "," + thisProject.id + " to parent: " + parentProject.name + "," + parentProject.id);
        }
        parentProject = thisProject;
    </script>
</#macro>

<#macro traverseAndRenderAppTreeData traversal>
    <@renderAppTreeData traversal.current traversal.canonicalProject />

    <#list sortProjectTraversalsByPathAscending(traversal.children) as childTraversal>
            <@traverseAndRenderAppTreeData childTraversal/>
    </#list>

    <script>
        parentProject = parentProject.getParent();
    </script>
</#macro>

<#macro traverseAndRenderProject traversal sha1ToPathsMapper>
    <@projectModelRenderer traversal sha1ToPathsMapper/>

    <#list sortProjectTraversalsByPathAscending(traversal.children) as childTraversal>
        <@traverseAndRenderProject childTraversal sha1ToPathsMapper/>
    </#list>
</#macro>

<#-- Renders the current project in the traversal -->
<#macro projectModelRenderer traversal sha1ToPathsMapper>
    <#assign projectModel = traversal.current>
    <#assign canonicalProject = traversal.canonicalProject>
    <#assign rootFilePath = traversal.getFilePath(projectModel.rootFileModel)>
    <#assign duplicatePaths = sha1ToPathsMapper.getPathsBySHA1(projectModel.rootFileModel.SHA1Hash)>
    <#assign isDuplicateProject = duplicatePaths?size &gt; 1>

	<#assign panelStoryPoints = getMigrationEffortPointsForProject(traversal, false, reportModel.includeTags, reportModel.excludeTags)>
    <#assign projectID = "project_${canonicalProject.asVertex().id?c}">
    <#assign classificationList = getClassificationForFile(projectModel.rootFileModel)>
    <div class="panel panel-primary projectBox" id="${projectID}" data-windup-projectguid="${generateGUID()}" data-windup-project-storypoints="${panelStoryPoints}">
        <div class="panel-heading panel-collapsed clickable">
            <span class="pull-left"><i class="glyphicon glyphicon-expand arrowIcon"></i></span>
            <h3 class="panel-title">
                <#if isDuplicateProject>
                    <span class="shared">[Included Multiple Times]</span> <span class="name">${projectModel.rootFileModel.fileName}</span>
                <#else>
                    <span class="name">${rootFilePath?html}</span>
                </#if>

                <span class="storyPoints">(<span class="points">${panelStoryPoints}</span> <span class="legend">story points</span>)</span>
                <span title="${(projectModel.rootFileModel.SHA1Hash[0..*8])!}">&nbsp;</span>
            </h3>
        </div>
        <div class="panel-body" style="display:none">
        <div class="container-fluid summaryMargin">

            <table class="summaryLayout">
                <tr>
                    <td colspan="2">
                        <!-- Points -->
                        <div class="points">
                            <div class="number">${panelStoryPoints}</div>
                            <div>Story Points</div>
                        </div>

                        <!-- Basic info -->
                        <div class="basicInfo">
                            <table class="table">
                                <#assign gav = canonicalProject.asVertex().getProperty('mavenIdentifier')! >
                                <#if gav?? >
                                <tr>
                                    <th>Maven coordinates</th>
                                    <td> ${gav?html} </td>
                                </tr>
                                </#if>
                                <tr>
                                    <th>Organization</th>
                                    <td>
                                        <#assign organizations = projectModelToOrganizations(canonicalProject)>
                                        <#if iterableHasContent(organizations)>
                                            <#list organizations.iterator() as organization>
                                                ${organization.name?html}
                                            </#list>
                                        </#if>
                                    </td>
                                </tr>
                                <tr>
                                    <th>Name</th>
                                    <td>${canonicalProject.name!""?html}</td>
                                </tr>
                                <tr>
                                    <th>Version</th>
                                    <td>${canonicalProject.version!""?html}</td>
                                </tr>
                                <tr>
                                    <th>Links</th>
                                    <td>
                                        <#if canonicalProject.url?has_content>
                                            <a href="${canonicalProject.url?html}">Project Site</a>
                                        </#if>

                                        <#if projectModelToSha1(canonicalProject)?has_content>
                                            <#assign sha1URL = '|ga|1|1:"' + projectModelToSha1(canonicalProject) + '"'>
                                            <#assign sha1URL = 'http://search.maven.org/#search' + sha1URL?url('ISO-8859-1')>
                                            <a href="${sha1URL?html}">Maven Central</a>
                                        </#if>
                                    </td>
                                </tr>
                                <tr>
                                    <th>Description</th>
                                    <td>
                                        ${canonicalProject.description!""}
                                    </td>
                                </tr>
                                <#if isDuplicateProject>
                                    <tr>
                                        <th>Duplicates</th>
                                        <td>
                                            <#list sortFilesByPathAscending(duplicatePaths) as path>
                                                <div>
                                                    ${path}
                                                </div>
                                            </#list>
                                        </td>
                                    </tr>
                                </#if>
                            </table>
                        </div><!-- /.basicInfo -->
                    </td>
                </tr>
                <tr>
                    <td>
                        <!-- Packages pie chart -->
                        <div class="chartBoundary">
                            <h4>Java Incidents by Package</h4>
                            <div id="project_${canonicalProject.asVertex().id?c}_pie" class="windupPieGraph"></div>
                        </div>
                    </td>
                    <td>
                        <div class="tagsBarChart chartBoundary">
                            <h4>Technologies found - occurrence count</h4>
                            <!-- Tags bar chart will be appended here. -->
                        </div>
                    </td>
                </tr>
            </table>

        </div>
        <#if iterableHasContent(classificationList)>
        <div>
            <#list classificationList.iterator() as classification>
                <div class="panel panel-default hint-detail-panel">
                    <div class="panel-heading">
                        <h4 class="panel-title pull-left">Issue Detail: ${classification.classification}</h4>
                            <div class="pull-right">
                                <a class="sh_url" title="${classification.ruleID}" href="windup_ruleproviders.html#${classification.ruleID}">Show Rule</a>
                            </div>
                        <div class="clearfix"></div>
                    </div>
                    <div class="panel-body">
                        ${markdownToHtml(classification.description)}
                    </div>
                    <#assign classificationLinkList = classification.links>
                    <#if iterableHasContent(classificationLinkList)>
                    <div class="panel-body">
                        <ul>
                        <#list classificationLinkList.iterator() as link>
                            <li><a href="${link.link}" target="_blank">${link.description}</a></li>
                        </#list>
                        </ul>
                    </div>
                    </#if>
                </div>
            </#list>
        </div>
        </#if>
        <#if iterableHasContent(canonicalProject.fileModelsNoDirectories)>
        <table class="subprojects table table-striped table-bordered">
            <tr>
                <th>Name</th><th>Technology</th><th>Issues</th><th>Story Points</th>
            </tr>
            <#list sortFilesByPathAscending(canonicalProject.fileModelsNoDirectories) as fileModel>
                <@fileModelRenderer fileModel/>
            </#list>
        </table>
        </#if>
        </div>
    </div>
</#macro>




<#-- ############################################################################################################### -->

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.rootFileModel.fileName?html} - Application Details Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <script src="resources/js/windup-overview-head.js"></script>
    <style>
        .desc { z-index: 5000; }
        
        /* Light yellow bg for the issue info box. */
        .hint-detail-panel > .panel-heading {
            border-color: #c2c2c2;
            background-color: #fbf4b1;
        }
        .hint-detail-panel {
            border-color: #a8d0e3;
            background-color: #fffcdc;
        }
        /* Reduce the padding, default is too big. */
        .hint-detail-panel > .panel-body { padding-bottom: 0; }
    </style>
</head>
<body role="document" class="java-application report-${reportModel.reportName}">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
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
                    <div class="main">Application Details Report</div>
                    <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                </h1>
                <div class="desc">
                    ${reportModel.description}
                </div>
            </div>
        </div>

        <!-- Summary -->
        <div class="row container-fluid">
            <div class="container mainGraphContainer">
                <table class="summaryLayout" style="width: 100%;">
                    <tr>
                        <td colspan="2">
                            <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                                <div class="number">${getMigrationEffortPointsForProject(allTraversal, true, reportModel.includeTags, reportModel.excludeTags)}</div>
                                <div>Story Points</div>
                            </div>
                            <div id="treeView-Projects-wrap" class="short">
                                <div id="overlayFog" class="showMore">
                                    <span style="position: relative; top: 4ex; left: 4em;" class="hideWhenComputed">Computing...</span>
                                </div>
                                <div id="treeView-Projects"></div>
                                <div class="showButtons hideUntilComputed">
                                    <a class="showMore" href="#" onclick='$("#treeView-Projects-wrap").removeClass("short")'>Show all &#x21F2;</a>
                                    <a class="showLess" href="#" onclick='$("#treeView-Projects-wrap").addClass("short")'>Show less &#x21F1;</a>
                                </div>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <div class="chartBoundary">
                                <h4>Java Incidents by Package</h4>
                                <div id="application_pie" class="windupPieGraph"></div>
                            </div>
                        </td>
                        <td>
                            <div class="chartBoundary">
                                <h4>Technologies found - occurrence count</h4>
                                <div id="tagsChartContainer-sum" style="height: 300px; width: 500px;">
                                    <div class="hideWhenComputed" style="position: relative; top: 4ex; left: 4em;">Computing...</div>
                                </div>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
        </div>

        <script>
            // For projects TreeView - used when gathering the projects data to JavaScript.
            var rootProject = null;
            var parentProject = null;
            var thisProject = null;
        </script>


        <div class="row container-fluid">
            <div class="theme-showcase" role="main">
                <@reportLineRenderer reportModel.applicationReportLines />
                <div>
                    <a id="collapseAll" href="javascript:collapseAll()">Collapse All</a>
                    <a id="expandAll" href="javascript:expandAll()">Expand All</a>
                </div>
                <#assign allTraversal = getProjectTraversal(reportModel.projectModel, 'all')>
                <#assign sha1ToPathsMapper = getArchiveSHA1ToPathMapper(allTraversal)>

                <@traverseAndRenderAppTreeData allTraversal/>
                <@traverseAndRenderProject getProjectTraversal(reportModel.projectModel, 'only_once') sha1ToPathsMapper/>

            </div> <!-- /container -->

            <#include "include/timestamp.ftl">
        </div>


        <script src="resources/js/jquery-1.10.1.min.js"></script>
        <script src="resources/js/jquery.storageapi.min.js"></script>
        <script src="resources/js/jquery.color-2.1.2.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
        <link   rel="stylesheet"      href="resources/libraries/jstree/themes/default/style.min.css"/>
        <script type="text/javascript" src="resources/libraries/jstree/jstree.min.js"></script>
        <script src="resources/js/windup-overview.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>

        <@render_pie projectTraversal=getProjectTraversal(reportModel.projectModel, 'only_once') recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

        <#macro projectPieRenderer projectTraversal>
            <@render_pie project=projectTraversal.current recursive=false elementID="project_${projectTraversal.canonicalProject.asVertex().id?c}_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

            <#list sortProjectTraversalsByPathAscending(projectTraversal.children) as childTraversal>
                <@projectPieRenderer childTraversal/>
            </#list>
        </#macro>

        <@projectPieRenderer getProjectTraversal(reportModel.projectModel, 'only_once') />


        <script>
            t0 = Date.now();
            expandMemory();
            console.log("PERF: expandMemory() took " + (Date.now() - t0) + " ms.");


            // Panels toggling - slide up or down.
            $(document).on("click", ".panel-heading", function(event) {
                togglePanelSlide.call(this, event);
            });
        </script>
        <script src="resources/tagsData.js"></script>
        <script>
            // Tags bar charts.
            $(document).ready( function() {
                window.tagService = new TagService();
                // The hierarchy of tags
                fillTagService(window.tagService);
                var chartObjects = {};
                createTagCharts();
            })


            // Projects TreeView.
            $(function() {
                window.setTimeout( function(){
                    t0 = Date.now();
                    renderAppTreeView(rootProject);
                    console.log("PERF: renderAppTreeView() took " + (Date.now() - t0) + " ms.");
                }, 500 );
            });
        </script>
    </div>
</body>
</html>
