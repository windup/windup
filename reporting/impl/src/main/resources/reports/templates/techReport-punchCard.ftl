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
    <style>
        /* Colors */
        .sectorView    { color: #1155CC; }
        .sectorConnect { color: #38761D; }
        .sectorStore   { color: #F4B400; }
        .sectorSustain { color: #DB4437; }
        .sectorExecute { color: #674EA7; }
        .sectorStats   { color: black; }

        table.technologiesPunchCard { border-collapse: collapse; }
        table.technologiesPunchCard td,
        table.technologiesPunchCard th {
            /*border: 1px solid silver;  /* debug */
        }
        tr.headersSector { font-size: 22pt; font-weight: bold; }
        tr.headersSector td { text-align: center; }

        tr.headersGroup  { font-size: 18pt; }
        tr.headersGroup td {
        }
        tr.headersGroup td div {
            height: 200px; /* Without this, the text is centered vertically. */
            width:   40px;
            padding: 0.5em 0;
            text-align: left;
            /*vertical-align: bottom; /* No effect. */
            writing-mode: vertical-lr; /* bt-lr doesn't work? So I turn it 180 with rotate() below */
            transform: rotate(180deg);
            white-space: nowrap;
        }

        tr.app { font-size: 12pt; }
        tr.app td.name,
        tr.app td.sectorStats { padding: 0 0.5em; }
        tr.app td.sectorStats { text-align: right; vertical-align: middle; }
        tr.app td.circle { text-align: center; vertical-align: middle; padding: 0; line-height: 1; }
        tr.app td.circle { font-size: 26pt; }
        tr.app td.circle.sizeX:after { content: "𐄂"; color: whitesmoke; font-size: 18pt; } /* No data */
        tr.app td.circle.size0:after { content: "⊘"; color: whitesmoke; font-size: 18pt; }
        tr.app td.circle.size1:after { content: "🞄"; }
        tr.app td.circle.size2:after { content: "⚫"; }
        tr.app td.circle.size3:after { content: "●"; }
        tr.app td.circle.size4:after { content: "⬤"; }
        tr.app td.circle.size5:after { content: "⬤"; } /* Should be 0-4, but just in case. */

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
                <#assign sectorTags = iterableToList(sectorTagsIterable) />
                <#assign sectorTags = sectorTags?sort_by("name") />

                <#-- MatrixAndMaximums {
                       countsOfTagsInApps,  // Map<ProjectModel, Map<String, Integer>>
                       maximumsPerTag       // Map<String, Integer>
                    }
                -->
                <#assign stats = getTechReportPunchCardStats() />

                <table class="technologiesPunchCard">
                    <tr class="headersSector">
                        <td></td>
                        <#list sectorTags as sector>
                            <td colspan="${iterableToList(sector.designatedTags)?size?c}" class="sector${sector.title}">${sector.title}</td>
                        <#else>
                            <td>No technology sectors defined.</td>
                        </#list>
                        <td colspan="3" class="sectorStats">Stats</td>
                    </tr>
                    <tr class="headersGroup">
                        <td class="sector"></td>
                        <#list sectorTags as sector>
                            <#list sector.designatedTags as tech>
                                <#assign techsOrder = techsOrder + [tech] />
                                <td class="sector${sector.title}"><div>${tech.title!}</div></td>
                            </#list>
                        </#list>
                        <td class="sectorStats sizeMB"><div>Size (MB)</div></td>
                        <td class="sectorStats libsCount"><div>Libraries</div></td>
                        <td class="sectorStats storyPoints"><div>Mandatory (SP)</div></td>
                    </tr>


                    <#list inputApplications as appProject> <#-- ProjectModel -->
                    <tr class="app">
                        <td class="name">${appProject.rootFileModel.fileName}</td>
                        <#list sectorTags as sector>
                            <#list sector.designatedTags as tech>
                                <#assign count = (stats.countsOfTagsInApps?api.get(appProject.asVertex().id)[tech.name])!false />
                                <#assign max = stats.maximumsPerTag[tech.name] />
                                <#assign log = count?is_number?then(getLogaritmicDistribution(count, max), false) />
                                <#if count?is_number >
                                    <!-- count: ${count}   max: ${max}   getLogaritmicDistribution(): ${ log } x 5 = ${ log * 5.0 } --><#-- Keep this here for debugging. -->
                                </#if>
                                <td class="circle size${ log?is_number?then((log * 5.0)?ceiling, "X")} sector${sector.title}"><!-- The circle is put here by CSS :after --></td>
                            <#else>
                                <td>No technology sectors defined.</td>
                            </#list>
                        </#list>
                        <td class="sectorStats sizeMB">
                            <#-- ${ (app.rootFileModel.retrieveSize() / 1024 / 1024)! } -->
                            ${ ( (appProject.rootFileModel.fileSize / 1024 / 1024)?string["0.##"] )! }
                        </td>
                        <td class="sectorStats libsCount">
                            ${ (appProject.getApplications()?size)! }
                        </td>
                        <td class="sectorStats storyPoints">
                            <#assign traversal = getProjectTraversal(appProject, 'only_once') />
                            <#assign panelStoryPoints = getMigrationEffortPointsForProject(traversal, false)! />
                            ${ panelStoryPoints! }
                        </td>
                    </tr>
                    </#list>
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

    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery-ui.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script src="resources/js/jquery.tablesorter.min.js"></script>
    <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
    <script src="resources/libraries/handlebars/handlebars.4.0.5.min.js"></script>
</body>
</html>
