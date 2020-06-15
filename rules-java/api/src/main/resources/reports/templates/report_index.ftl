<!DOCTYPE html>
<html lang="en">

<#if reportModel.applicationReportIndexModel??>
    <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
</#if>


<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.rootFileModel.applicationName?html} - ${reportModel.reportName}</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link rel="stylesheet" type="text/css" href="resources/libraries/snippet/jquery.snippet.min.css" />
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link rel="stylesheet" type="text/css" href="resources/libraries/sausage/sausage.css" />
    <link rel="stylesheet" type="text/css" href="resources/libraries/flot/plot.css" />
    <link href="resources/img/mta-icon.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
.report-index-row {
    margin: 10px -32px 0px 5px;
    margin-bottom: 25px;
}
    </style>
</head>
<body role="document" class="java-report-index">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
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
                    <div class="main">${reportModel.reportName}
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="${reportModel.description}"></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row container-fluid col-md-12 summaryInfo">
            <div class="panel panel-primary col-md-12">
                <div class="row col-md-12 report-index-row">
                    <div class="col-md-3">
                        <table class="table table-condensed table-striped" id="incidentsByTypeTable">
                            <thead>
                                <tr>
                                    <td>
                                        <b>Incidents by Category</b>
                                    </td>
                                    <td class='numeric-column'>
                                        <b>Incidents</b>
                                    </td>
                                    <td class='numeric-column'>
                                        <b>Total Story Points</b>
                                    </td>
                                </tr>
                            </thead>
                            <tbody id="incidentsByTypeTBody">
                            </tbody>
                        </table>
                    </div>

                    <div class="col-md-8">
                        <div class="col-md-6">
                            <div style="text-align: center"><strong>Incidents by Category</strong></div>
                            <div id="incidentsBySeverityChart" style="float: left;"></div>
                        </div>
                        <div class="col-md-6">
                            <div style="text-align: center"><strong>Incidents and Story Points</strong></div>
                            <div id="effortAndSeverityChart" style="float: right;"></div>
                        </div>
                    </div>
                </div>

                <div class="row col-md-12 report-index-row">
                    <div class="col-md-3">
                        <table class="table table-condensed table-striped">
                            <thead>
                                <tr>
                                    <td>
                                        <b>Mandatory Incidents by Type</b>
                                    </td>
                                    <td class='numeric-column'>
                                        <b>Incidents</b>
                                    </td>
                                    <td class='numeric-column'>
                                        <b>Total Story Points</b>
                                    </td>
                                </tr>
                            </thead>
                            <tbody id="mandatoryIncidentsByEffortTBody">
                            </tbody>
                        </table>
                    </div>
                    <div class="col-md-8">
                        <div class="col-md-6">
                            <div style="text-align: center"><strong>Mandatory Incidents by category</strong></div>
                            <div id="mandatoryIncidentsByEffort" style="float: left;"></div>
                        </div>
                        <div class="col-md-6">
                            <div style="text-align: center"><strong>Mandatory Incidents and Story Points</strong></div>
                            <div id="mandatoryIncidentsByEffortAndStoryPoints" style="float: right;"></div>
                        </div>
                    </div>
                </div><#-- .row -->

                <div class="row col-md-12 report-index-row" id="javaIncidentsByPackageRow">
                    <div class="col-md-3">
                        <table class="table table-condensed table-striped">
                            <thead>
                                <tr>
                                    <td>
                                        <b>Java Incidents by Package</b>
                                    </td>
                                    <td class='numeric-column'>
                                        <b>Incidents</b>
                                    </td>
                                </tr>
                            </thead>
                            <tbody id="javaIncidentsByPackageTBody">
                            </tbody>
                        </table>
                        <span class="note">Note: this does not include XML files and "possible" issues.</span>
                    </div>
                    <div class="panel col-md-6">
                        <div style="margin-bottom: 10px; margin-left: 190px;">
                            <b>Java Incidents by Package</b>
                        </div>
                        <div id='application_pie' class='windupPieGraph'></div>
                    </div>
                </div><#-- .row -->
            </div><#-- .panel -->
        </div><#-- .row.summaryInfo -->

        <div class="row col-md-12">
        <#include "include/timestamp.ftl">
        </div>
    </div>

    <script type="text/javascript" src="resources/js/jquery-3.3.1.min.js"></script>
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
            return WINDUP_ISSUE_SUMMARIES['${reportModel.projectModel.getElement().id()?c}'];
        }
    </script>

    <script type="text/javascript" src="resources/js/report-index-graphs.js"></script>

    <@render_pie projectTraversal=getProjectTraversal(reportModel.projectModel, 'only_once') recursive=true elementID="application_pie" includeTags=reportModel.includeTags excludeTags=reportModel.excludeTags />

    <script type="text/javascript">
        var tbodyElement = $("#javaIncidentsByPackageTBody");

        <#-- Make sure that the data exists before trying to use it -->
        if (typeof(WINDUP_PACKAGE_PIE_DATA) !== 'undefined') {
            var rows = "";
            for (var i = 0; i < WINDUP_PACKAGE_PIE_DATA['application_pie'].length; i++) {
                var row = "";
                row += "<tr>";
                row += "<td>" + WINDUP_PACKAGE_PIE_DATA['application_pie'][i].label + "</td>";
                row += "<td class='numeric-column'>" + WINDUP_PACKAGE_PIE_DATA['application_pie'][i].data + "</td>";
                row += "</tr>";
                rows += row;
            }
            tbodyElement.prepend(rows);
        } else {
            $("#javaIncidentsByPackageRow").remove();
        }
    </script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
