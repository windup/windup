<!DOCTYPE html>
<html lang="en">

<#if reportModel.applicationReportIndexModel??>
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>


<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.rootFileModel.fileName?html} - ${reportModel.reportName}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link rel='stylesheet' type='text/css' href='resources/libraries/snippet/jquery.snippet.min.css' />
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link rel='stylesheet' type='text/css' href='resources/css/windup-source.css' />
    <link rel='stylesheet' type='text/css' href='resources/libraries/sausage/sausage.css' />
    <link rel='stylesheet' type='text/css' href='resources/libraries/flot/plot.css' />
</head>
<body role="document" class="java-report-index">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
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
                    <div class="main" onmouseover="$(this).parent().parent().addClass('showDesc')" onmouseout=" $(this).parent().parent().removeClass('showDesc')">
                        ${reportModel.reportName}
                    </div>
                    <div class="path">${reportModel.projectModel.rootFileModel.fileName}</div>
                </h1>
                <div class="desc" style="z-index: 9000">
                    ${reportModel.description}
                </div>
            </div>
        </div>

        <div class="row container-fluid col-md-12">
            <div class="row-eq-height">
                <div class="panel panel-primary col-md-6">
                    <table class="table table-condensed table-striped" id="incidentsByTypeTable">
                        <thead>
                            <tr>
                                <td>
                                    <b>Incidents by Category</b>
                                </td>
                                <td>
                                    <b>Incidents</b>
                                </td>
                                <td>
                                    <b>Total Story Points</b>
                                </td>
                            </tr>
                        </thead>
                        <tbody id="incidentsByTypeTBody">
                            <tr>
                                <td colspan="3" style="text-align: center;">
                                    <div class="row container-fluid">
                                        <div id="incidentsBySeverityChart" style="float: left;">
                                        </div>
                                        <div id="effortAndSeverityChart" style="float: right;">
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="panel panel-primary col-md-6">
                    <table class="table table-condensed table-striped">
                        <thead>
                            <tr>
                                <td>
                                    <b>Mandatory Incidents by Type</b>
                                </td>
                                <td>
                                    <b>Incidents</b>
                                </td>
                                <td>
                                    <b>Total Story Points</b>
                                </td>
                            </tr>
                        </thead>
                        <tbody id="mandatoryIncidentsByEffortTBody">
                            <tr>
                                <td colspan="3" style="text-align: center;">
                                    <div class="row container-fluid">
                                        <div id="mandatoryIncidentsByEffort" style="float: left;">
                                        </div>
                                        <div id="mandatoryIncidentsByEffortAndStoryPoints" style="float: right;">
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
            <div class="panel panel-primary col-md-12">
                <div class="panel col-md-6">
                    <div style="margin-left: 190px;">
                        <b>Java Incidents by Package</b>
                    </div>
                    <div id='application_pie' class='windupPieGraph'/>
                </div>
                <div class="panel col-md-6"></div>
            </div>
        </div>

        <div class="row container-fluid col-md-12">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    Additional Reports
                </div>
                <table class="table table-condensed table-striped">
                    <tbody>
                        <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as navReportModel>
                            <#if navReportModel.displayInApplicationReportIndex>
                                <#assign reportUrl = navReportModel.reportFilename>
                                <#if navUrlPrefix??>
                                    <#assign reportUrl = "${navUrlPrefix}${reportUrl}">
                                </#if>

                                <#if !reportModel.equals(navReportModel)>
                                    <tr>
                                        <td class="col-md-2">
                                            <a href="${reportUrl}">${navReportModel.reportName}</a>
                                        </td>
                                        <td class="col-md-10">
                                            ${navReportModel.description!""}
                                        </td>
                                    </tr>
                                </#if>
                            </#if>
                        </#list>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <script type="text/javascript" src="resources/js/jquery-1.10.1.min.js"></script>
    <script src="resources/js/jquery.color-2.1.2.min.js"></script>
    <script src="resources/js/bootstrap.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.valuelabels.js"></script>
    <script src="resources/libraries/flot/jquery.flot.axislabels.js"></script>
    <script src="resources/libraries/flot/jquery.flot.resize.js"></script>

    <script type="text/javascript" src="data/issue_summaries.js"></script>

    <script type="text/javascript">
        function getWindupIssueSummaries() {
            return WINDUP_ISSUE_SUMMARIES['${reportModel.projectModel.asVertex().id?c}'];
        }
    </script>

    <script type="text/javascript" src="resources/js/report-index-graphs.js"></script>

    <@render_pie project=reportModel.projectModel recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />
</body>
</html>
