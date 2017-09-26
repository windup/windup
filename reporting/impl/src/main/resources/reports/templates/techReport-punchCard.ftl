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
            border: 1px solid silver;
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
        tr.app td.circle.size0:after { content: "🞄"; }
        tr.app td.circle.size1:after { content: "⚫"; }
        tr.app td.circle.size2:after { content: "●"; }
        tr.app td.circle.size3:after { content: "⬤"; }
        tr.app td.circle.size4:after { content: "⬤"; } /* Should be 0-3, but just in case. */
        tr.app td.circle.sizeX:after { content: "𐄂"; }

    </style>

    <script>
        /**
         * @param count    Count of occurences
         * @param maximum  The maximal count of occurences across apps.
         * @returns {number} the number usable for circle size CSS style, currently 0-3.
         *          0 for 0, 1 under roughly 20 %, 2 under roughly 65%, 3 for the rest.
         */
        function getCircleSize(count, maximum) {
            if (count < 1) return 0;
            var ratio = count / maximum;
            // Map it to scale 0..200. This "spreads" the curve so we get 3 a bit later than at 10%.
            var ratio2 = 1 + ratio * 199;
            var log10 = Math.log10(ratio2);
            return Math.min(3, Math.ceil(log10)); // Top it at 3
        }
    </script>
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

                <pre>
                    reportModel.maximumCounts: ${mapToJsonMethod(reportModel.maximumCounts)}
                </pre>

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
                    </tr>


                    <#list inputApplications as app> <#-- ProjectModel -->
                    <tr class="app">
                        <td class="name">${app.rootFileModel.fileName}</td>
                        <#list sectorTags as sector>
                            <#list sector.designatedTags as tech>
                                <#assign count = (stats.countsOfTagsInApps?api.get(app.asVertex().id)[tech.name])! />

                                <#assign max = stats.maximumsPerTag[tech.name] />
                                <td class="circle size${ (count??)?then(getLogaritmicDistribution(count, max) * 4, "X")} sector${sector.title}"><!-- The circle is put here by CSS :after --></td>
                            <#else>
                                <td>No technology sectors defined.</td>
                            </#list>
                            <td class="sectorStats sizeMB"></td>
                            <td class="sectorStats libsCount"></td>
                            <td class="sectorStats storyPoints"></td>
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
    <script src="resources/js/jquery.tablesorter.min.js"></script>
    <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
    <script src="resources/libraries/handlebars/handlebars.4.0.5.min.js"></script>
</body>
</html>
