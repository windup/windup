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
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link href="resources/css/jquery-ui.min.css" rel="stylesheet" media="screen">
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <link href="resources/css/tech-report-punchcard.css" rel="stylesheet">
</head>
<body role="document">
    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
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
                    <div class="main">${reportModel.reportName}</div>
                    <#if reportModel.projectModel??>
                        <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                    </#if>
                </h1>
                <div class="desc">
                    ${reportModel.description}
                </div>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

                <#assign techsOrder = [] />

                <#assign sectorTagsIterable = reportModel.sectorsHolderTag.designatedTags />
                <#assign sectorTags = iterableToList(sectorTagsIterable) />
                <#assign sectorTags = sectorTags?sort_by("name") />
                <#assign placeTagsParent = getTagModelByName("techReport:mappingOfPlacementTagNames") />

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
                            <td colspan="${ (iterableToList(sector.designatedTags)?size-1)?c}" class="sector${sector.title}">${sector.title}</td>
                        <#else>
                            <td>No technology sectors defined.</td>
                        </#list>
                            <td colspan="3" class="sectorStats">Stats</td>
                            <!-- this td is needed for scrollbar positioning -->
                            <td class="scrollbar-padding"></td>
                        </tr>
                        <tr class="headersGroup">
                            <td></td>
                        <#list sectorTags as sectorTag >
                            <#list sectorTag.designatedTags as boxTag >
                                <#if !isTagUnderTag(boxTag, placeTagsParent) >
                                    <#assign techsOrder = techsOrder + [boxTag] />
                                    <td class="sector sector${sectorTag.title}"><div>${boxTag.title!}</div></td>
                                </#if>
                            </#list>
                        </#list>
                            <td class="sector sectorStats sizeMB"><div>Size (MB)</div></td>
                            <td class="sector sectorStats libsCount"><div>Libraries</div></td>
                            <td class="sector sectorStats storyPoints"><div>Mandatory (SP)</div></td>
                            <!-- this td is needed for scrollbar positioning -->
                            <td class="scrollbar-padding"></td>
                        </tr>
                    </thead>
                    <tbody>
                        <#list inputApplications as appProject> <#-- ProjectModel -->
                        <#if appProject.projectType! != "VIRTUAL" >
                        <tr class="app">
                            <td class="name">
                                <#assign boxReport = reportModel.appProjectIdToReportMap[appProject.getElement().id()?c] > <#-- TechReportModel -->
                                <a href="${boxReport.reportFilename}">
                                    <#-- For virtual apps, use name rather than the file name. -->
                                    ${ (appProject.projectType! = "VIRTUAL" && appProject.name??)?then(
                                            appProject.name,
                                            appProject.rootFileModel.fileName)}
                                </a>
                            </td>
                            <#list sectorTags as sectorTag>
                                <#list sectorTag.designatedTags as boxTag>
                                    <#if !isTagUnderTag(boxTag, placeTagsParent) >
                                        <#--
                                        <#assign count = (stats.countsOfTagsInApps?api.get(appProject.getElement().id())[boxTag.name])!false />
                                        <#assign maxForThisBox = stats.maximumsPerTag[boxTag.name] />
                                        -->

                                        <#-- 2nd way - using the 4 layer map -->
                                        <#assign statsForThisBox = sortedStatsMatrix.get("", boxTag.name, appProject.getElement().id()?long)! />
                                        <#assign count = (statsForThisBox[""].occurrenceCount)!false />
                                        <#assign maxForThisBox   = (sortedStatsMatrix.getMaxForBox(boxTag.name))!false />
                                        <#assign isBooleanTech = maxForThisBox?is_number && maxForThisBox == 0 />
                                        <#if isBooleanTech>
                                            <!-- The boolean technologies will either be missing or present. Presence is denoted by 0. Use some middle bubble size for present. -->
                                            <#assign log = count?is_number?then(0.5, 0) />
                                        <#else>
                                            <#-- If the tech did not appear in any TechUsageStats, it is missing in the map. -->
                                            <#if count?is_number && maxForThisBox?is_number >
                                                <#assign log = getLogaritmicDistribution(count, maxForThisBox) />
                                            <#else>
                                                <#assign log = 0 />
                                            </#if>
                                        </#if>
                                        <!-- count: ${count?c}   max: ${maxForThisBox?c}   getLogaritmicDistribution(): ${ log?c } x 5 = ${ log * 5.0 } -->

                                        <td class="circle size${ log?is_number?then((log * 5.0)?ceiling, "X")} sector sector${sectorTag.title}"><#-- The circle is put here by CSS :after --></td>
                                    </#if>
                                <#else>
                                    <td>No technology sectors defined.</td>
                                </#list>
                            </#list>
                            <td class="sectorStats sector sizeMB">
                                ${ ( (appProject.rootFileModel.retrieveSize() / 1024 / 1024)?string["0.##"] )! }
                            </td>
                            <td class="sectorStats libsCount">
                                <#assign noOfLibraries = getNumberOfLibraries(appProject) />
                                ${ noOfLibraries! }
                            </td>
                            <td class="sectorStats storyPoints">
                                <#assign traversal = getProjectTraversal(appProject, 'all') />
                                <#assign mandatoryCategory = ["mandatory"] />
                                <#assign panelStoryPoints = getMigrationEffortPointsForProject(traversal, true, [], [], mandatoryCategory)! />
                                ${ panelStoryPoints! }
                            </td>
                            <!-- this td is needed for scrollbar positioning -->
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

    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>
