<#ftl output_format="HTML">

<#include "include/effort_util.ftl">

<#if reportModel.applicationReportIndexModel??>
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>
        <#if reportModel.projectModel??>
            ${reportModel.projectModel.name} -
        </#if>
        ${reportModel.reportName}
    </title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/css/jquery-ui.min.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <link href="resources/css/tech-report-punchcard.css" rel="stylesheet">
</head>
<body role="document">
    <#-- Navbar -->
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
    <#-- / Navbar -->

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">${reportModel.reportName}
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="${reportModel.description}"></i></div>
                    <#if reportModel.projectModel??>
                        <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                    </#if>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

                <#assign techsOrder = [] />
                <#assign sectorTags = iterableToList(reportModel.sectorsHolderTag.designatedTags) />
                <#assign sectorTags = sectorTags?sort_by("name") />
                <#assign placeTagsParent = getTagModelByName("techReport:mappingOfPlacementTagNames") />
                <#assign sortedRowTags = iterableToList(reportModel.rowsHolderTag.designatedTags) />
                <#assign sortedRowTags = sortedRowTags?sort_by("title") />


            <#-- MatrixAndAggregated {
                   countsOfTagsInApps,  // Map<ProjectModel, Map<String, Integer>>
                   maximumsPerTag       // Map<String, Integer>
                }
            <#assign stats = getTechReportPunchCardStats() />
            -->

                <#-- A precomputed matrix - map of maps of maps, boxTag -> rowTag -> project -> techName -> TechUsageStat.
                     Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>> -->
                <#assign sortedStatsMatrix = sortTechUsageStats() />


                <table class="technologiesPunchCard">
                    <thead>
                        <tr class="headersSector">
                            <td></td>
                        <#list sectorTags as sector>
                            <td colspan="${ (iterableToList(sector.designatedTags)?size-1)?c}" class="sectorHeader sector${sector.title}"><div>${sector.title}</div></td>
                        <#else>
                            <td>No technology sectors defined.</td>
                        </#list>
                            <td colspan="3" class="sectorStats">Stats</td>
                            <#-- this td is needed for scrollbar positioning -->
                            <td class="scrollbar-padding"></td>
                        </tr>
                        <tr class="headersGroup">
                            <td class="sector"><div>Name</div></td>
                        <#list sectorTags as sectorTag >
                            <#assign sortedBoxTags = iterableToList(sectorTag.designatedTags)?sort_by("title") />
                            <#list sortedBoxTags as boxTag >
                                <#if !isTagUnderTag(boxTag, placeTagsParent) >
                                    <#assign techsOrder = techsOrder + [boxTag] />
                                    <td class="sector sector${sectorTag.title}"><div style="width: 100%">${boxTag.title!}</div></td>
                                </#if>
                            </#list>
                        </#list>
                            <td class="sector sectorStats sizeMB"><div>Size (MB)</div></td>
                            <td class="sector sectorStats libsCount"><div>Libraries</div></td>
                            <td class="sector sectorStats storyPoints"><div>Mandatory (SP)</div></td>
                            <td class="sector sectorStats storyPoints"><div>Cloud Mandatory (SP)</div></td>
                            <td class="sector sectorStats storyPoints"><div>Potential (Count)</div></td>
                            <#-- this td is needed for scrollbar positioning -->
                            <td class="scrollbar-padding"></td>
                        </tr>
                    </thead>
                    <tbody>
                        <#assign appProjects = inputApplications?sort_by(["name"]) />
                        <#list appProjects as appProject> <#-- ProjectModel -->
                        <#if appProject.projectType! != "VIRTUAL" >
                        <tr class="app">
                            <td class="name sector sectorSummary">
                                <#assign boxReport = reportModel.appProjectIdToReportMap[appProject.getElement().id()?c] > <#-- TechReportModel -->
                                <a href="${boxReport.reportFilename}">
                                    <#-- For virtual apps, use name rather than the file name. -->
                                    ${ (appProject.projectType! = "VIRTUAL" && appProject.name??)?then(
                                            appProject.name,
                                            appProject.rootFileModel.applicationName)}
                                </a>
                            </td>
                            <#list sectorTags as sectorTag>

                                <#assign countSector = 0 />
                                <#assign sortedBoxTags = iterableToList(sectorTag.designatedTags)?sort_by("title") />
                                <#list sortedBoxTags as boxTag>
                                    <#if !isTagUnderTag(boxTag, placeTagsParent) >
                                        <#--
                                        <#assign count = (stats.countsOfTagsInApps?api.get(appProject.getElement().id())[boxTag.name])!false />
                                        <#assign maxForThisBox = stats.maximumsPerTag[boxTag.name] />
                                        -->
                                        <#-- 2nd way - using the 4 layer map -->
                                        <#assign statsForThisBox = sortedStatsMatrix.get("", boxTag.name, appProject.getElement().id()?long)! />
                                        <#assign count = (statsForThisBox[""].occurrenceCount)!false />
                                        <#assign countInteger = count?is_number?then(count, 0) />
                                        <#assign countSector += countInteger />
                                        <#assign maxForThisBox   = (sortedStatsMatrix.getMaxForBox(boxTag.name))!false />
                                        <#assign isBooleanTech = maxForThisBox?is_number && maxForThisBox == 0 />
                                        <#if isBooleanTech>
                                            <#-- The boolean technologies will either be missing or present. Presence is denoted by 0. Use some middle bubble size for present. -->
                                            <#assign log = count?is_number?then(0.5, 0) />
                                        <#else>
                                            <#-- If the tech did not appear in any TechUsageStats, it is missing in the map. -->
                                            <#if count?is_number && maxForThisBox?is_number >
                                                <#assign log = getLogaritmicDistribution(count, maxForThisBox) />
                                            <#else>
                                                <#assign log = 0 />
                                            </#if>
                                        </#if>
                                        <#-- count: ${count?c}   max: ${maxForThisBox?c}   getLogaritmicDistribution(): ${ log?c } x 5 = ${ log * 5.0 } -->
                                        <td class="circle size${ log?is_number?then((log * 5.0)?ceiling, "X")} sector sector${sectorTag.title} table-tooltip" data-count="${countInteger?c}">
                                            <#-- The circle is put here by CSS :after -->
                                            <#if countInteger gt 0>
                                                <div class="table-tooltiptext">
                                                        <#assign itemisedStatsForThisBox = (sortedStatsMatrix.getSummarizedStatsByTechnology(boxTag.name, appProject.getElement().id()?long))! />
                                                        <#assign countTooltipRows = 0 />
                                                        <#list itemisedStatsForThisBox>
                                                            <#items as name, stat>
                                                                <#if (stat.occurrenceCount > 0) >
                                                                    <div class="row">
                                                                        <#if (countTooltipRows == 0)>
                                                                        <div class="tooltiptext-tech-name-header col-md-9">Total</div><div class="tooltiptext-tech-count-header col-md-3">${stat.occurrenceCount}</div>
                                                                        <#else>
                                                                        <div class="tooltiptext-tech-name col-md-9">${stat.name}</div><div class="tooltiptext-tech-count col-md-3">${stat.occurrenceCount}</div>
                                                                        </#if>
                                                                    </div>
                                                                    <#assign countTooltipRows += 1 />
                                                                </#if>
                                                            </#items>
                                                        </#list>
                                                </div>
                                            </#if>
                                        </td>
                                    </#if>
                                <#else>
                                    <td>No technology sectors defined.</td>
                                </#list>
                            <td class="sector${sectorTag.title}Summary sectorSummary" data-count="${countSector?c}" hidden></td>
                            </#list>
                            <#if isFileADirectory(appProject.rootFileModel)>
                            <td class="sectorStats sector sizeMB" data-count="${(appProject.rootFileModel.getDirectorySize()?c)!0}">
                                ${ ( (appProject.rootFileModel.getDirectorySize() / 1024 / 1024)?string["0.##"] )! }
                            </td>
                            <#else>
                            <td class="sectorStats sector sizeMB" data-count="${(appProject.rootFileModel.retrieveSize()?c)!0}">
                                ${ ( (appProject.rootFileModel.retrieveSize() / 1024 / 1024)?string["0.##"] )! }
                            </td>
                            </#if>
                            <#assign noOfLibraries = getNumberOfLibraries(appProject) />
                            <td class="sectorStats libsCount" data-count="${noOfLibraries?c}">
                                ${ noOfLibraries! }
                            </td>

                            <#assign traversal = getProjectTraversal(appProject, 'all') />

                            <#assign mandatoryCategory = ["mandatory"] />
                            <#assign cloudMandatoryCategory = ["cloud-mandatory"] />
                            <#assign potentialIssuesCategory = ["potential"] />

                            <#assign mandatoryStoryPoints = getMigrationEffortPointsForProject(traversal, true, [], [], mandatoryCategory)! />
                            <td class="sectorStats storyPoints" data-count="${mandatoryStoryPoints?c}">
                                ${ mandatoryStoryPoints! }
                            </td>

                            <#assign cloudMandatoryStoryPoints = getMigrationEffortPointsForProject(traversal, true, [], [], cloudMandatoryCategory)! />
                            <td class="sectorStats storyPoints" data-count="${cloudMandatoryStoryPoints?c}">
                                ${ cloudMandatoryStoryPoints! }
                            </td>

                            <#assign incidentCountByCategory = getEffortCountForProjectByIssueCategory(event, traversal, true)>
                            <#assign potentialFound = false>
                            <#list incidentCountByCategory?keys as issueCategory>
                                <#if issueCategory.categoryID == "potential">
                                    <#assign potentialFound = true>
                                    <#assign potentialIncidents = incidentCountByCategory?api.get(issueCategory) >
                                    <td class="sectorStats storyPoints" data-count="${potentialIncidents?c}">
                                        ${ potentialIncidents! }
                                    </td>
                                </#if>
                            </#list>
                            <#if !potentialFound>
                                <td class="sectorStats storyPoints" data-count="0">
                                    0
                                </td>
                            </#if>

                            <#-- this td is needed for scrollbar positioning -->
                            <td class="scrollbar-padding"></td>
                        </tr>
                        </#if>
                        </#list>
                    </tbody>
                </table>

            </div>
        </div>
    </div>
    <#-- Keep this here for debugging.
    <pre>
        <#list 0..7 as count>
        ${count?string(000)} / 7  =>   ${getLogaritmicDistribution(count, 7)}
        </#list>
        <#list 0..9 as i>
            <#assign count = i * 50 + 1 />
        ${count?string(000)} / 500 =>  ${getLogaritmicDistribution(count, 500)}
        </#list>
    </pre>
    -->
    <script src="resources/js/jquery-3.3.1.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
    <script>
        var currentSortColumn = null;
        var currentSortSector = null;
        var reverse = false;
        var reverseSector = false;

        function sortTableByColumn(column) {
            // cleanup the previous sort classes
            var groupedHeaderColumns = $('.headersGroup td').get();
            groupedHeaderColumns.forEach(function(columnHeaderElement) {
                $(columnHeaderElement).removeClass("sectorSorted");
                $(columnHeaderElement).removeClass("sort_asc");
                $(columnHeaderElement).removeClass("sort_desc");
            });

            var groupedSectors = $('.headersSector .sectorHeader').get();
            groupedSectors.forEach(function(sectorHeaderElement) {
                $(sectorHeaderElement).removeClass("sectorSorted");
                $(sectorHeaderElement).removeClass("sort_asc");
                $(sectorHeaderElement).removeClass("sort_desc");
            });

            if (column == currentSortColumn) {
                reverse = !reverse;
            } else {
                reverse = false;
                currentSortColumn = column;
            }
            var reverseSortFactor = reverse ? 1 : -1;

            $(groupedHeaderColumns[column]).addClass("sectorSorted");
            $(groupedHeaderColumns[column]).addClass(reverse ? "sort_asc" : "sort_desc");

            var rows = $('.technologiesPunchCard tbody tr').get();

            rows.sort(function(a, b) {

                var A = getVal(a);
                var B = getVal(b);
                if (!$.isNumeric(A) || !$.isNumeric(B)) {
                    return B.localeCompare(A) * reverseSortFactor;
                }

                if(A < B) {
                    return -1*reverseSortFactor;
                }
                if(A > B) {
                    return 1*reverseSortFactor;
                }
                return 0;
            });

            function getVal(elm) {
                var v = $(elm).children('.sector').eq(column).data("count");
                if (v == null) {
                    v = $(elm).children('.sector').eq(column).text().trim();
                } else if($.isNumeric(v)) {
                    v = parseInt(v,10);
                }
                return v;
            }

            $.each(rows, function(index, row) {
                $('.technologiesPunchCard').children('tbody').append(row);
            });
        }

        function sortTableBySector(sector) {
            // cleanup the previous sort classes
            var groupedHeaders = $('.headersGroup td').get();
            groupedHeaders.forEach(function(columnHeaderElement) {
                $(columnHeaderElement).removeClass("sectorSorted");
                $(columnHeaderElement).removeClass("sort_asc");
                $(columnHeaderElement).removeClass("sort_desc");
            });

            var groupedSectors = $('.headersSector td').get();
            groupedSectors.forEach(function(sectorHeaderElement) {
                $(sectorHeaderElement).removeClass("sectorSorted");
                $(sectorHeaderElement).removeClass("sort_asc");
                $(sectorHeaderElement).removeClass("sort_desc");
            });

            if (sector == currentSortSector) {
                reverseSector = !reverseSector;
            } else {
                reverseSector = false;
                currentSortSector = sector;
            }
            var reverseSortFactor = reverseSector ? 1 : -1;

            $(groupedSectors[sector]).addClass("sectorSorted");
            $(groupedSectors[sector]).addClass(reverseSector ? "sort_asc" : "sort_desc");

            var rows = $('.technologiesPunchCard tbody tr').get();

            rows.sort(function(a, b) {

                var A = getVal(a);
                var B = getVal(b);
                if (!$.isNumeric(A) || !$.isNumeric(B)) {
                    return B.localeCompare(A) * reverseSortFactor;
                }

                if(A < B) {
                    return -1*reverseSortFactor;
                }
                if(A > B) {
                    return 1*reverseSortFactor;
                }
                return 0;
            });

            function getVal(elm) {
                var v = $(elm).children('.sectorSummary').eq(sector).data("count");
                if (v == null) {
                    v = $(elm).children('.sectorSummary').eq(sector).text().trim();
                } else if($.isNumeric(v)) {
                    v = parseInt(v,10);
                }
                return v;
            }

            $.each(rows, function(index, row) {
                $('.technologiesPunchCard').children('tbody').append(row);
            });
        }

        $().ready(function () {
            $(".headersGroup .sector").click(function (event) {
                var td = event.target.parentNode;
                var index = $(td).index();
                sortTableByColumn(index);
            });
            reverse = true;
            currentSortColumn = 0;
            sortTableByColumn(0);
        });

        $().ready(function () {
            $(".headersSector .sectorHeader").click(function (event) {
                var td = event.target.parentNode;
                var index = $(td).index();
                sortTableBySector(index);
            });
            reverseSector = true;
            currentSortSector = 0;
            sortTableBySector(0);
        });
    </script>
</body>
</html>