<#--ftl output_format="HTML"-->

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

    <#assign basePath="resources">
    <#include "include/favicon.ftl">

    <style>
        /* Colors. */
        <#-- TODO: These should be generated from the techSector:* tags, but was good enough for initial impl. -->
        .sectorView    div { background-color: #1155CC; color: white; }
        .sectorConnect div { background-color: #38761D; color: white; }
        .sectorStore   div { background-color: #F4B400; color: white; }
        .sectorSustain div { background-color: #DB4437; color: white; }
        .sectorExecute div { background-color: #674EA7; color: white; }
        .sectorStats   div { background-color: white; color: black; }

        /* Partition, sector, group */

        table.technologiesBoxCard { border-collapse: collapse; width: 90%; margin: 10pt auto; }
        table.technologiesBoxCard td,
        table.technologiesBoxCard th { border: 0px solid silver; }
        table.technologiesBoxCard td.heading { border-bottom-width: 1px; border-bottom-color:  #d9d9d9; }

        /* Sector headers */
        tr.sectorsHeaders { font-size: 20pt; font-weight: bold; }
        tr.sectorsHeaders > td { text-align: center; padding: 10pt 20pt 0; } /* Around the sector header box. */
        tr.rowSectors > td { padding: 2ex 2em; }
        tr.sectorsHeaders > td div { text-align: center; padding: 1ex 2em; }

        /* Partitions = gray areas */
        tr.rowHeader { font-size: 14pt; font-weight: bold; }
        tr.rowHeader > td > div { background-color: #D9D9D9; padding: 1ex 20pt 0; margin-top: 18pt; }
        /*tr.rowHeader td,*/
        tr.rowSectors > td { background-color: #D9D9D9; padding: 1ex 2em; vertical-align: top; }
        tr.rowSectors > td  > div.box { padding: 8pt 18pt 8pt 12pt; margin-bottom: 10pt; }
        tr.rowSectors > td  > div.box > h4 { font-size: 12pt; font-weight: bold; }

        tr.rowSectors .box h4  { font-size: 12pt; font-weight: bold; text-align: right; }
        tr.rowSectors .box ul li  { font-size: 12px; }

        tr.rowSectors .box .icon {
            float: left;
            width: 70px; height: 70px;
            background-image: url("resources/icons/techreport/Object_Shield-CoatOfArms.png");
            background-repeat: no-repeat;
            background-size: contain;
            background-position: center;
        }
        tr.rowSectors .box .content { margin-left: 62px; }
        tr.rowSectors .box .content ul { list-style: none; margin: 0; -webkit-padding-start: 10px; }
        tr.rowSectors .box .content ul li { margin: 0; text-align: right; }
        tr.rowSectors .box .content ul li b { width: 3ex; display: inline-block; }
        tr.headersGroup { height: 215px; }
    </style>

    <script src="resources/js/jquery-3.3.1.min.js"></script>
</head>

<body role="document">
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

                <#assign sectorTagsIterable = reportModel.sectorsHolderTag.designatedTags />
                <#assign sectorTags = iterableToList(sectorTagsIterable) /> <#-- Later: FM 2.3.27 introduces ?sequence -->
                <#assign sectorTags = sectorTags?sort_by("name") />

                <#-- A precomputed matrix - map of maps of maps, boxTag -> rowTag -> project -> techName -> TechUsageStat.
                     Map<String, Map<String, Map<Long, Map<String, TechReportService.TechUsageStatSum>>>> -->
                <#assign sortedStatsMatrix = sortTechUsageStats(reportModel.projectModel) />

                <table class="technologiesBoxCard">
                    <tr class="sectorsHeaders">
                        <#list sectorTags as sectorTag>
                            <td class="sector${sectorTag.title}"><div>${sectorTag.title}</div></td>
                        <#else>
                            <td>No technology sectors defined.</td>
                        </#list>
                    </tr>

                    <!-- For each gray row group... -->
                    <#assign sortedRowTags = reportModel.rowsHolderTag.designatedTags?sort_by("title") />
                    <#assign rowTags = sortedRowTags?reverse />
                    <#list rowTags as rowTag> <#-- currently Java EE / Embedded -->
                        <tr class="rowHeader row-${rowTag.name}">
                            <td class="heading" colspan="${sectorTags?size}"><div>${rowTag.title}</div></td>
                        </tr>
                        <tr class="rowSectors">
                            <#list sectorTags as sectorTag>
                                <td class="sector${sectorTag.title}">
                                    <#assign boxTags = iterableToList(sectorTag.designatedTags) >
                                    <#assign boxTags = boxTags?sort_by("name") >
                                    <#list boxTags as boxTag>
                                        <#if isTagUnderTag(boxTag, rowTag)>
                                            <#-- Get a map of box buckets with TechUsageStats and take data from there, rather than pulling through a function. -->
                                            <#assign statsForThisBox = (sortedStatsMatrix.get(rowTag.name, boxTag.name, 0))! />
                                            <#list statsForThisBox>
                                                <div class="box box-${boxTag.name}" id="box${boxTag.getElement().id()?c}">
                                                    <div class="icon icon-${(boxTag.traits["icon"])!}"
                                                         style="background-image: url('resources/icons/techreport/${(boxTag.traits["icon"])!}.png');"></div>
                                                    <div class="content">
                                                        <h4>${boxTag.titleOrName}</h4>
                                                        <ul>
                                                            <#items as name, stat>
                                                                <li>
                                                                    ${stat.name}
                                                                    <#if (stat.occurrenceCount > 0) >
                                                                        <b>${stat.occurrenceCount}</b>
                                                                    </#if>
                                                                </li>
                                                            </#items>
                                                        </ul>
                                                    </div>
                                                    <div style="clear: both;"></div>
                                                </div>
                                            </#list>
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


    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
