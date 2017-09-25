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
            xvertical-align: bottom; /* No effect. */
            writing-mode: vertical-lr; /* bt-lr doesn't work? So I turn it 180 with rotate() later */
            transform: rotate(180deg);
            white-space: nowrap;
        }

        tr.app { font-size: 18pt; }
        tr.app td.name,
        tr.app td.sectorStats { padding: 0 0.5em; }
        tr.app td.sectorStats { text-align: right; vertical-align: middle; }
        tr.app td.circle { text-align: center; vertical-align: middle; padding: 0; line-height: 1; }
        tr.app td.circle { font-size: 26pt; }
        tr.app td.circle.size0:after { content: "🞄"; }
        tr.app td.circle.size1:after { content: "⚫"; }
        tr.app td.circle.size2:after { content: "●"; }
        tr.app td.circle.size3:after { content: "⬤"; }

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

                <#assign sectorTagsIterable = reportModel.sectorsHolderTag.designatedTags>
                <#assign sectorTags = iterableToList(sectorTagsIterable) />
                <#assign sectorTags = sectorTags?sort_by("name") />

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
                    </tr>
                    <tr class="headersGroup">
                        <td class="sector"></td>
                        <#list sectorTags as sector>
                            <#list sector.designatedTags.iterator() as tech>
                                <#assign techsOrder = techsOrder + [tech] />
                                <td class="sector${sector.title}"><div>${tech.title!}</div></td>
                            </#list>
                        </#list>
                    </tr>

                    <#-- FileModel, probably ApplicationArchiveModel. -->
                    <#list inputPaths.iterator() as app>
                    <tr class="app">
                        <td class="name">${app.fileName}</td>
                        <#list sectorTags as sector>
                            <#list sector.designatedTags.iterator() as tech>
                                <#--
                                <td class="circle size${getCircleSize(app, tech)} sector${sector.name}"><!-- The circle is put here by CSS :after - -></td>
                                -->
                                <td class="circle size3 sector${sector.title}"><!-- The circle is put here by CSS :after --></td>
                            <#else>
                                <td>No technology sectors defined.</td>
                            </#list>
                        </#list>
                    </tr>
                    </#list>
                </table>

                <#-- Map<ProjectModel, Map<String, Integer>>
                <#assign matrix = getTechReportPunchCardStats()>
                MAIN CONTENT HERE 2
                 -->

                <!-- /// Mock -->
                <table class="technologiesPunchCard">
                    <tr class="headersSector">
                        <td></td>
                        <td colspan="2" class="sectorView">View</td>
                        <td colspan="4" class="sectorConnect">Connect</td>
                        <td colspan="3" class="sectorStore">Store</td>
                        <td colspan="5" class="sectorSustain">Sustain</td>
                        <td colspan="2" class="sectorExecute">Execute</td>
                        <td colspan="3" class="sectorStats">Stats</td>
                    </tr>
                    <tr class="headersGroup">
                        <td class="sector"></td>
                        <td class="sectorView"><div>Web</div></td>
                        <td class="sectorView"><div>Rich</div></td>
                        <td class="sectorConnect"><div>EJB</div></td>
                        <td class="sectorConnect"><div>WS / REST</div></td>
                        <td class="sectorConnect"><div>JMS / MDB</div></td>
                        <td class="sectorConnect"><div>JNI</div></td>
                        <td class="sectorStore"><div>ORM</div></td>
                        <td class="sectorStore"><div>JDBC</div></td>
                        <td class="sectorStore"><div>Cache</div></td>
                        <td class="sectorSustain"><div>Transactions</div></td>
                        <td class="sectorSustain"><div>Clustering</div></td>
                        <td class="sectorSustain"><div>Security</div></td>
                        <td class="sectorSustain"><div>Logging</div></td>
                        <td class="sectorSustain"><div>Test</div></td>
                        <td class="sectorExecute"><div>IoC</div></td>
                        <td class="sectorExecute"><div>3rd party</div></td>
                        <td class="sectorStats"><div>Size (MB)</div></td>
                        <td class="sectorStats"><div>Libraries</div></td>
                        <td class="sectorStats"><div>Mandatory (SP)</div></td>
                    </tr>
                    <tr class="app">
                        <td class="name">App1.ear</td>
                        <td class="circle size3 sectorView"></td>
                        <td class="circle size0 sectorView"></td>
                        <td class="circle size0 sectorConnect"></td>
                        <td class="circle size0 sectorConnect"></td>
                        <td class="circle size0 sectorConnect"></td>
                        <td class="circle size0 sectorConnect"></td>
                        <td class="circle size2 sectorStore"></td>
                        <td class="circle size2 sectorStore"></td>
                        <td class="circle size2 sectorStore"></td>
                        <td class="circle size0 sectorSustain"></td>
                        <td class="circle size0 sectorSustain"></td>
                        <td class="circle size0 sectorSustain"></td>
                        <td class="circle size0 sectorSustain"></td>
                        <td class="circle size0 sectorSustain"></td>
                        <td class="circle size0 sectorExecute"></td>
                        <td class="circle size2 sectorExecute"></td>
                        <td class="sectorStats">10.3</td>
                        <td class="sectorStats">23</td>
                        <td class="sectorStats">313</td>
                    </tr>
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
