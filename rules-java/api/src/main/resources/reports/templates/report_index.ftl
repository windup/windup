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
    <link rel="stylesheet" type="text/css" href="resources/libraries/snippet/jquery.snippet.min.css" />
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen">
    <link rel="stylesheet" type="text/css" href="resources/libraries/sausage/sausage.css" />
    <link rel="stylesheet" type="text/css" href="resources/libraries/flot/plot.css" />
    <link href="resources/img/rhamt-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
    <style>
        .report-index-row {
            margin: 10px -32px 0px 5px;
            margin-bottom: 25px;
        }
        .dataTable { max-width: 500px; margin-top: 2ex; }
        div.panel-collapsed { display: none; }
    </style>
</head>
<body role="document" class="java-report-index">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-default navbar-fixed-top">
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

        <div class="row container-fluid summaryInfo col-md-12">            
            <div class="row report-index-row col-md-12">
                <!-- Java incidents by package chart -->
                <div class="chartBoundary col-md-4">
                  <h4>Java Incidents by Package</h4>
                  <div id='application_pie' class='windupPieGraph'></div>
                </div>
                <!-- end of Java Package usage -->

                <!-- Incidents and story points chart -->
                <div class="chartBoundary col-md-4">
                    <h4>Incidents and Story Points</h4>
                    <div id="effortAndCategoryChart"></div>
               </div>
                <!-- end of Incidents and story Points chart -->

                <!-- Mandatory incidents by Effort -->
                <div class="chartBoundary col-md-4">
                    <h4>Mandatory Incidents and Story Points</h4>
                    <div id="mandatoryIncidentsByEffortChart"></div>
                </div>
            </div>

            <!-- second row with tables -->
             <div class="row report-index-row col-md-12">
                <div class="panel panel-heading clickable" style="cursor: pointer;">
                    <span id="hide" style="display: none;"><i class="glyphicon glyphicon-expand"></i> Hide Details</span>
                    <span id="show"><i class="glyphicon glyphicon-collapse-down"></i> Show Details</span>
               </div>

                <!-- Java package usage table DETAILS -->
                <div class="panel-body details panel-collapsed col-md-4" id="javaIncidentsByPackageRow">
                    <table class="table table-condensed table-striped  dataTable">
                        <#--caption>Java Packages Usage</caption-->
                         <thead>
                             <tr>
                             <th>Java Package</th>
                                <th class='numeric-column'>Incidents</th>
                             </tr>
                         </thead>
                          <tbody id="javaIncidentsByPackageTBody">
                         </tbody>
                     </table>
                    <span class="note">Note: this does not include XML files and "possible" issues.</span>
                </div>
                <!-- end of Java package usage table DETAILS -->
                
                <div class="panel-body details panel-collapsed col-md-4">
                    <table class="table table-condensed table-striped dataTable" id="incidentsByTypeTable">
                        <#--caption>Incidents by Category</caption-->
                        <thead>
                            <tr>
                                <th>Category</th>
                                <th class='numeric-column'>Incidents</th>
                                <th class='numeric-column'>Total Story Points</th>
                            </tr>
                        </thead>
                        <tbody id="incidentsByTypeTBody">
                        </tbody>
                    </table>
                </div>

                <!-- Mandatory incidents table DETAILS -->
                <div class="panel-body details panel-collapsed col-md-4">
                    <table class="table table-condensed table-striped dataTable">
                        <#-- caption>Mandatory Incidents by Type</caption-->
                        <thead>
                            <tr>
                                <th>Effort Level</th>
                                <th class='numeric-column'>Incidents</th>
                                <th class='numeric-column'>Total Story Points</th>
                            </tr>
                        </thead>
                        <tbody id="mandatoryIncidentsByEffortTBody">
                        </tbody>
                    </table>
                </div>
            </div><#-- .row -->
        </div><#-- .row.summaryInfo -->

        <div class="row col-md-12">
        <#include "include/timestamp.ftl">
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
        // function for collapsing/expanding of summary table details
        $(document).ready(function(){
            $('#hide').click(function(){
                $('.details').addClass('.panel-collapsed');
                $('.details').hide();
                $('#show').show();
                $('#hide').hide();
            });
            $("#show").click(function(){
                $('.details').removeClass('.panel-collapsed');
                $('.details').show();
                $('#show').hide();
                $('#hide').show();
            });
        });

        function getWindupIssueSummaries() {
            return WINDUP_ISSUE_SUMMARIES['${reportModel.projectModel.asVertex().id?c}'];
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
</body>
</html>
