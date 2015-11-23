<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>


<#macro tagRenderer tag>
    <span class="label label-${(tag.level! == 'IMPORTANT')?then('danger','info')} tag-${tag.name?replace(' ','')}">
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
            <a href="${sourceReportModel.reportFilename}">
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


<#macro projectModelRenderer projectModel>
    <div class="panel panel-primary projectBox" id="project_${projectModel.asVertex().id?c}">
        <#assign panelStoryPoints = getMigrationEffortPoints(projectModel, false, reportModel.includeTags, reportModel.excludeTags)>
        <div class="panel-heading panel-collapsed clickable">
            <span class="pull-left"><i class="glyphicon glyphicon-expand arrowIcon"></i></span>
            <h3 class="panel-title">${projectModel.rootFileModel.prettyPath?html} (${panelStoryPoints} story points)</h3>
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
                                <tr>
                                    <th>Organization</th>
                                    <td>
                                        <#assign organizations = projectModelToOrganizations(projectModel)>
                                        <#if iterableHasContent(organizations)>
                                            <#list organizations.iterator() as organization>
                                                ${organization.name?html}
                                            </#list>
                                        </#if>
                                    </td>
                                </tr>
                                <tr>
                                    <th>Version</th>
                                    <td>${projectModel.name!""?html}</td>
                                </tr>
                                <tr>
                                    <th>Links</th>
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
                                    <td>
                                        ${projectModel.description!""}
                                    </td>
                                </tr>
                            </table>
                        </div><!-- /.basicInfo -->
                    </td>
                </tr>
                <tr>
                    <td>
                        <!-- Packages pie chart -->
                        <div class="chartBoundary">
                            <h4>Incompatible API usage count (by API packages)</h4>
                            <div id="project_${projectModel.asVertex().id?c}_pie" class="windupPieGraph"></div>
                        </div>
                    </td>
                    <td>
                        <div class="tagsBarChart chartBoundary">
                            <h4>Technologies found - occurence count</h4>
                            <!-- Tags bar chart will be appended here. -->
                        </div>
                    </td>
                </tr>
            </table>

        </div>
        <#if iterableHasContent(projectModel.fileModelsNoDirectories)>
        <table class="subprojects table table-striped table-bordered">
            <tr>
                <th>Name</th><th>Technology</th><th>Issues</th><th>Story Points</th>
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
    <title>${reportModel.projectModel.name?html} - Application Report</title>
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
                    <div class="path">${reportModel.projectModel.name?html}</div>
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
            <div class="container mainGraphContainer">
                <table style="width: 100%;">
                    <tr>
                        <td>
                            <div class="points" style="text-align: center; color: #00254b; padding-bottom: 1ex;">
                                <div class="number">${getMigrationEffortPoints(reportModel.projectModel, true, reportModel.includeTags, reportModel.excludeTags)}</div>
                                <div>Story Points</div>
                            </div>
                        </td>
                        <td>
                            <div class="chartBoundary">
                                <h4>Incidents distribution amongst packages</h4>
                                <div id="application_pie" class="windupPieGraph"/>
                            </div>
                        </td>
                    </tr>
                    <tr>
                        <td>
                        </td>
                        <td>
                            <div class="chartBoundary">
                                <h4>Technologies found</h4>
                                <div id="tagsChartContainer-sum" style="height: 300px; width: 500px;"></div>
                            </div>
                        </td>
                    </tr>
                </table>
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
        <script src="resources/js/jquery.color-2.1.2.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.min.js"></script>
        <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
        <script src="resources/js/windup-overview.js"/>
        <script src="resources/js/bootstrap.min.js"></script>

        <@render_pie project=reportModel.projectModel recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

        <#macro projectPieRenderer projectModel>
            <@render_pie project=projectModel recursive=false elementID="project_${projectModel.asVertex().id?c}_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

            <#list projectModel.childProjects.iterator() as childProject>
                <@projectPieRenderer childProject />
            </#list>
        </#macro>

        <@projectPieRenderer reportModel.projectModel />


        <script>
        // Panels toggling - slide up or down.
        $(document).on("click", ".panel-heading", function(event) {
            togglePanelSlide.call(this, event);
            //initializeBarchartIfNotDoneYet.call(this, event);
        });
        $('#collapseAll').toggle();

        $( document ).ready( function () {
            createTagCharts();
        })

        var chartObjects = {};

        function initializeBarchartIfNotDoneYet(event) {
            $(this).parents(".projectBox").each(function(){
                var chartObj = chartObjects[ $(this).attr("id") + "-tags" ];
                chartObj.setupGrid();
                chartObj.draw();
            });
        }

        function createTagCharts() {
            // For each project's file, count it's tags.
            var rootProjCountMap = {};
            // Append a div to each project's summary. Doesn't work because it's hidden while Flot is rendering.
            //$(".projectBox .summaryMargin"/* td.tagsBarChart*/).append('<div class="tagChart" style="height: 300px; width: 600px;"></div>')
            $(".projectBox").each(function(iProj){
                var projectId = $(this).attr("id");
                curProjCountsMap = {};

                // Create the tag -> count map.
                $(this).find(".projectFile").each(function(iFile){
                    $(this).find(".tech .label").each(function(iTag){
                        var tagName = $(this).text().trim();
                        rootProjCountMap[ tagName ] = ++rootProjCountMap[ tagName ] || 1; // Sum map.
                        curProjCountsMap[ tagName ] = ++curProjCountsMap[ tagName ] || 1;
                    })
                })

                // Don't draw the chart if there's just one tag.
                if (curProjCountsMap.length < 2)
                    return;

                chartHeight = Math.max(50, Math.min(400, Object.keys(curProjCountsMap).length * 22 + 20));

                // Render the bar chart for this project.
                // We need to render it somewhere where it is visible and then move to the collapsed subproject divs.
                $(document.body).append('<div class="tagChart" style="height: ' + chartHeight + 'px; width: 500px;"></div>'); // Returns body.
                var chartDiv = $("body > .tagChart")[0];
                curProjCountsMap = sortMapByValues(curProjCountsMap);
                // Store the chart object for later use.
                chartObjects[projectId + "-tags"] =
                        createChart(chartDiv, curProjCountsMap);
                $("body > .tagChart").appendTo( $(this).find(".summaryMargin .tagsBarChart") );
                $(this).find(".summaryMargin .tagsBarChart").append( $("body > .tagChart") );
            });

            // Sum tags chart
            rootProjCountMap = sortMapByValues(rootProjCountMap);
            createChart("#tagsChartContainer-sum", rootProjCountMap);

            // Substitutes yaxis: { font: } - Flot uses "smaller" which breaks alignment.
            $(".tagChart .flot-text").css("font-size", "");
        }


        // Prepare the data in the format [[value,index], ...].
        function prepareFlotData(tagToCountMap) {
            var ticks = [];
            var values = [];
            var maxValue = 1;

            var keys = getKeys(tagToCountMap);
            for (var i = 0; i < keys.length; i++) {
                ticks[i] = [i, keys[i]];
                values[i] = [tagToCountMap[keys[i]], i];
                maxValue = Math.max(maxValue, values[i][0]);
            }
            return { ticks: ticks, values: values, maxValue: maxValue };
        }

        function createChart(divSelectorOrElement, tagToCountMap) {
            var flotData = prepareFlotData(tagToCountMap);
            return createFlotChart(divSelectorOrElement, flotData);
        }

        function createFlotChart(divSelectorOrElement, flotData) {
            var dataset = [{ data: flotData.values, color: "#5482FF" }];

            var options = {
                series: { bars: { horizontal: true, show: true } },
                bars: {
                    align: "center",
                    barWidth: 0.6,
                    lineWidth: 1,
                },
                grid: {
                    hoverable: true,
                    borderWidth: 1,
                    borderColor: "#B0B0B0",
                    backgroundColor: { colors: ["#FFFFFF", "#EDF5FF"] },
                    margin: 3, // Doesn't work
                    minBorderMargin: 3,
                },
                xaxis: {
                    axisLabel: "Count",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: "Verdana, Arial",
                    axisLabelPadding: 10,
                    // Logarithmic
                    //ticks: [1,2,3,4,5,6,7,8,9,10,15,30,50,75,100,200,350,500,750,1000,5000,10000],
                    //transform: function(v) { return Math.log(v+0.0001); /*move away from zero*/},
                    //inverseTransform: function (v) { return Math.exp(v); }
                    //min: 0.7,
                    max: flotData.maxValue * 1.1, // Substitutes grid: { margin: ... }
                    tickDecimals: 0,
                    tickFormatter: function(value, axis){ return value + "x"; },
                },
                yaxis: {
                    axisLabel: "Technology",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: "Verdana, Arial",
                    ticks: flotData.ticks,
                    // Otherwise Flot uses "smaller" which breaks alignment.
                    //font: { size: "14px", color: "black" }, // doesn't work
                },
            };

            return $.plot( $(divSelectorOrElement), dataset, options );
        }
        </script>
    </div>
</body>
</html>
