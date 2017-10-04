<#ftl output_format="HTML">

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
    <style>
        /* Colors */
        .sectorView    div { background-color: #1155CC; color: white; }
        .sectorConnect div { background-color: #38761D; color: white; }
        .sectorStore   div { background-color: #F4B400; color: white; }
        .sectorSustain div { background-color: #DB4437; color: white; }
        .sectorExecute div { background-color: #674EA7; color: white; }
        .sectorStats   div { background-color: white; color: black; }

        /* Partition, sector, group */

        table.technologiesBoxCard { border-collapse: collapse; width: 90%; margin: 10pt auto; }
        table.technologiesBoxCard td,
        table.technologiesBoxCard th {
            border: 1px solid silver;
        }

        /* Sector headers */
        tr.sectorsHeaders { font-size: 22pt; font-weight: bold; }
        tr.sectorsHeaders > td { text-align: center; padding: 2ex 20pt; } /* Around the box */
        tr.rowSectors > td { padding: 2ex 2em; }
        tr.sectorsHeaders > td div { text-align: center; padding: 1ex 2em; }

        /* Partitions = gray areas */
        tr.rowHeader { font-size: 22pt; font-weight: bold; }
        tr.rowHeader > td > div { background-color: #D9D9D9; padding: 1ex 20pt 0; margin-top: 18pt; }
        /*tr.rowHeader td,*/
        tr.rowSectors > td { background-color: #D9D9D9; padding: 1ex 2em; vertical-align: top; }
        tr.rowSectors > td  > div { padding: 1ex 20pt; margin-bottom: 10pt; }
        tr.rowSectors > td  > div > h4 { font-size: 14pt; font-weight: bold; }

        tr.rowSectors h4  { font-size: 18pt; font-weight: bold; }
        tr.rowSectors ul li  { font-size: 12pt; }
    </style>
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
                <#assign sectorTags = iterableToList(sectorTagsIterable) /> <#-- Later: FM 2.3.27 introduces ?sequence -->
                <#assign sectorTags = sectorTags?sort_by("name") />

                <#-- MatrixAndAggregated {
                       countsOfTagsInApps,  // Map<ProjectModel, Map<String, Integer>>
                       maximumsPerTag       // Map<String, Integer>
                       totalsPerTag         // Map<String, Integer>
                    }
                -->
                <#assign stats = getTechReportPunchCardStats() />

                <table class="technologiesBoxCard">
                    <tr class="sectorsHeaders">
                        <#list sectorTags as sectorTag>
                            <td class="sector${sectorTag.title}"><div>${sectorTag.title}</div></td>
                        <#else>
                            <td>No technology sectors defined.</td>
                        </#list>
                    </tr>

                    <!-- For each gray row group... -->
                    <#assign rowTags = reportModel.rowsHolderTag.designatedTags />
                    <#list rowTags as rowTag> <#-- currently Java EE / Embedded -->
                        <tr class="rowHeader row-${rowTag.name}">
                            <td class="heading" colspan="${sectorTags?size}"><div>${rowTag.title}</div></td>
                        </tr>
                        <tr class="rowSectors">
                            <#list sectorTags as sectorTag>
                                <td class="sector${sectorTag.title}">
                                    <#list sectorTag.designatedTags as boxTag>
                                        <#if isTagUnderTag(boxTag, rowTag)>
                                        <div class="box box-${boxTag.name} #box${boxTag.asVertex().id}">
                                            <div class="icon">[icon]</div>
                                            <h4>${boxTag.titleOrName}</h4>
                                            <ul>
                                                <#--
                                                <#list boxTag.designatedTags as techTag>
                                                    <#assign count = (stats.totalsPerTag[techTag.name])!0 />
                                                    <li class="stats tag-${techTag.name} count${count?switch(0, '0', 1, '1', 'Many')}">
                                                        ${techTag.titleOrName} <b>${count}</b>
                                                    </li>
                                                </#list>
                                                -->
                                                <#-- Get the individual techs under this sector and row. JSF, JSP, Servlet, ... etc.
                                                -->
                                                <#-- Set<TechnologyUsageStatisticsModel> -->
                                                <#-- Map<String, TechUsageStatSum> -->
                                                <#assign techUsageStats = getTechnologiesIdentifiedForSubSectorAndRow(boxTag, rowTag, reportModel.project) />
                                                <#-- TODO: Get a map of box buckets with TechUsageStats and take data from there, rather than pulling through a function. -->
                                                <#list techUsageStats as name, techUsageStatSum>
                                                    <li class="stats count${techUsageStatSum.occurrenceCount!0?switch(0, '0', 1, '1', 'Many')}">
                                                        ${techUsageStatSum.name}
                                                        <b>${techUsageStatSum.occurrenceCount!}</b>
                                                    </li>
                                                </#list>
                                            </ul>
                                            <script></script>
                                        </div>
                                        </#if>
                                    </#list>
                                </td>
                            </#list>
                        </tr>
                    </#list>

                </table>

            </div>
        </div>
    </div>

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery-ui.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>
