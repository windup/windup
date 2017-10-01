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
        tr.partitionSectors > td { padding: 2ex 2em; }
        tr.sectorsHeaders > td div { text-align: center; padding: 1ex 2em; }

        /* Partitions = gray areas */
        tr.partitionHeader { font-size: 22pt; font-weight: bold; }
        tr.partitionHeader > td > div { background-color: #D9D9D9; padding: 1ex 20pt 0; margin-top: 18pt; }
        /*tr.partitionHeader td,*/
        tr.partitionSectors > td { background-color: #D9D9D9; padding: 1ex 2em; vertical-align: top; }
        tr.partitionSectors > td  > div { padding: 1ex 20pt; margin-bottom: 10pt; }
        tr.partitionSectors > td  > div > h4 { font-size: 14pt; font-weight: bold; }

        tr.partitionSectors h4  { font-size: 18pt; font-weight: bold; }
        tr.partitionSectors ul li  { font-size: 12pt; }
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
                        <tr class="partitionHeader partition${rowTag.title}">
                            <td class="heading" colspan="${sectorTags?size}"><div>${rowTag.title}</div></td>
                        </tr>
                        <tr class="partitionSectors">
                            <#list sectorTags as sectorTag>
                                <td class="sector${sectorTag.title}">
                                    <#list sectorTag.designatedTags as boxTag>
                                        <div class="box box-${boxTag.name}">
                                            <div class="icon">[icon]</div>
                                            <h4>${boxTag.titleOrName}</h4>
                                            <ul>
                                                <#-- Get the individual techs under this sector and row. JSF, JSP, Servlet, ... etc.
                                                <#assign techIdentAndCount = GetTechnologiesIdentifiedForSubSectorAndRow(boxTag, rowTag, reportModel.project) />
                                                -->
                                                <#list boxTag.designatedTags as techTag>
                                                    <#assign count = (stats.totalsPerTag[techTag.name])!0 />
                                                    <li class="stats tag-${techTag.name} count${count?switch(0, '0', 1, '1', 'Many')}">
                                                        ${techTag.titleOrName} <b>${count}</b>
                                                    </li>
                                                </#list>
                                            </ul>
                                        </div>
                                    </#list>
                                </td>
                            </#list>
                        </tr>
                    </#list>

                </table>

            </div>
        </div>
    </div>



    <!-- ================= Mockup ==================== -->

    <table class="technologiesBoxCard">
        <tr class="sectorsHeaders">
            <td class="sectorView"><div>View</div></td>
            <td class="sectorConnect"><div>Connect</div></td>
            <td class="sectorStore"><div>Store</div></td>
            <td class="sectorSustain"><div>Sustain</div></td>
            <td class="sectorExecute"><div>Execute</div></td>
        </tr>

        <!-- Gray area -->
        <tr class="partitionHeader partitionJavaEE">
            <td class="heading" colspan="5"><div>Java EE</div></td>
        </tr>
        <tr class="partitionSectors">
            <td class="sectorView">
                <div>
                    <div class="icon"></div>
                    <h4>Web</h4>
                    <ul>
                        <li>JSF</li>
                        <li>JSP</li>
                        <li>Servlet</li>
                        <li>web.xml</li>
                        <li>WebSocket</li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Rich</h4>
                    <ul>
                        <li>applet</li>
                        <li>JNLP</li>
                    </ul>
                </div>
            </td>
            <td class="sectorConnect">
                <div>
                    <div class="icon"></div>
                    <h4>Messaging</h4>
                    <ul>
                        <li>JMS queue <b>6</b></li>
                        <li>JMS topic <b>0</b></li>
                        <li>JMS con. factory <b>2</b></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Bean</h4>
                    <ul>
                        <li>Stateless (SLSB) <b>23</b></li>
                        <li>Stateful (SFSB)  <b>2</b></li>
                        <li>Message (MDB)    <b>0</b></li>
                        <li>Managed Bean     <b>0</b></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>HTTP</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Other</h4>
                    <div>
                        <ul>
                            <li></li>
                        </ul>
                    </div>
                </div>
            </td>
            <td class="sectorStore">
                <div>
                    <div class="icon"></div>
                    <h4> Database</h4>
                    <ul>
                        <li>JDBC datasource   5</li>
                        <li>JDBC XA datasource   0</li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Persistence</h4>
                    <ul>
                        <li>Persistence units      3</li>
                        <li>JPA entities   54</li>
                        <li>JPA named queries      9</li>
                    </ul>
                </div>
            </td>
            <td class="sectorSustain">
                <div>
                    <div class="icon"></div>
                    <h4>Transactions</h4>
                    <div>
                        <ul>
                            <li></li>
                        </ul>
                    </div>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Clustering</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Security</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Logging</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Test</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </td>
            <td class="sectorExecute">
                <div>
                    <div class="icon"></div>
                    <h4>IoC</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>3rd party</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </td>
        </tr>

        <!-- ================== -->
        <tr class="partitionHeader partitionEmbedded">
            <td class="heading" colspan="5"><div>Embedded</div></td>
        </tr>

        <tr class="partitionSectors">
            <td class="sectorView">
                <div>
                    <div class="icon"></div>
                    <h4>MVC</h4>
                    <ul>
                        <li>Spring-MVC</li>
                        <li>Struts</li>
                        <li>Wicket</li>
                        <li>GWT</li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Markup</h4>
                    <ul>
                        <li>CSS</li>
                        <li>JS</li>
                    </ul>
                </div>
            </td>
            <td class="sectorConnect">
                <div>
                    <div class="icon"></div>
                    <h4>Web Service</h4>
                    <ul>
                        <li>Axis</li>
                        <li>CXF</li>
                        <li>XFire</li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>REST</h4>
                    <ul>
                        <li>Jersey</li>
                        <li>Unirest</li>
                        <li>...</li>
                    </ul>
                </div>
            </td>
            <td class="sectorStore">
                <div>
                    <div class="icon"></div>
                    <h4>ORM</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>JDBC</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Cache</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </td>
            <td class="sectorSustain">
                <div>
                    <div class="icon"></div>
                    <h4>Transactions</h4>
                    <div>
                        <ul>
                            <li></li>
                        </ul>
                    </div>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Clustering</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Security</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Logging</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>Test</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </td>
            <td class="sectorExecute">
                <div>
                    <div class="icon"></div>
                    <h4>IoC</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
                <div>
                    <div class="icon"></div>
                    <h4>3rd party</h4>
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </td>
        </tr>
    </table>




    <script src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery-ui.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
</body>
</html>
