<!DOCTYPE html>

<#macro migrationIssuesRenderer problemSummary>
    <tr class="problemSummary effort${getEffortDescriptionForPoints(problemSummary.effortPerIncident, 'id')}">
        <td>
            <a href="#" class="toggle">
                <#assign issueName = problemSummary.issueName!"No name">
                ${issueName?html}
            </a>
        </td>
        <td class="text-right">${problemSummary.numberFound}</td>
        <td class="text-right">${problemSummary.effortPerIncident}</td>
        <td class="level">${getEffortDescriptionForPoints(problemSummary.effortPerIncident, 'verbose')}</td>
        <td class="text-right">${problemSummary.numberFound * problemSummary.effortPerIncident}</td>
    </tr>
    <tr class="tablesorter-childRow bg-info" data-summary-id="${problemSummary.id}">
        <td colspan="5" style="display: none;" class="table-inner-wrapping-td">
            <table class="table-inner table table-bordered table-condensed migration-issues-table">
                <thead>
                    <!-- First rows used to calculate columns width -->
                    <tr style="visibility: collapse;"><th></th><th></th><th></th><th></th><th></th></tr>
                    <tr class="bg-info">
                        <th><div class="indent"><strong>File</strong></div></th>
                        <th class="text-right"><strong>Incidents Found</strong></th>
                        <th colspan="3"><strong>Hint</strong></th>
                    </tr>
                </thead>
                <tbody>

                </tbody>
            </table>
        </td>
    </tr>
</#macro>

<#function getIncidentsFound problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + problemSummary.numberFound>
    </#list>
    <#return result>
</#function>

<#function getTotalPoints problemSummaries>
    <#assign result = 0>
    <#list problemSummaries as problemSummary>
        <#assign result = result + (problemSummary.numberFound * problemSummary.effortPerIncident)>
    </#list>
    <#return result>
</#function>

<html lang="en">
    <#if reportModel.applicationReportIndexModel??>
        <#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>
    </#if>

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
            .table-bordered { border: 1px solid #222222;  border-collapse: collapse;}

            .table-bordered>tfoot>tr>th,
            .table-bordered>tfoot>tr>td .table-bordered>tbody>tr>th,
            .table-bordered>thead>tr>th,
            .table-bordered>thead>tr>td,
            .table-bordered>tbody>tr>td {
              border: 1px inset #dddddd
            }

            .fileSummary {
                background-color: #f5f5f5;
            }
            /* Only horizontal lines. */
            .migration-issues-table.table-bordered > thead > tr > th,
            .migration-issues-table.table-bordered > tbody > tr > td {
                border-left-style: none;
                border-right-style: none;
            }

            /* Light yellow bg for the issue info box. */
            .hint-detail-panel > .panel-heading {
                border-color: #c2c2c2;
                background-color: #fbf4b1;
            }

            .hint-detail-panel {
                border-color: #c2c2c2;
                background-color: #fffcdc;
            }
            /* Reduce the padding, default is too big. */
            .hint-detail-panel > .panel-body { padding-bottom: 0; }

            .technology {
                display: inline-block;
                padding: 5px;
                margin: auto;
                border-radius: 3px;
                color: white;
            }

            .source {
                background-color: green;
            }

            .target {
                background-color: orange;
            }

            .tech-list {
                margin-left: 5px;
            }

            /* Colors of various effort levels. */
            /* Commented out for now (jsight - 2016/02/15)
            tr.problemSummary.effortINFO td.level { color: #1B540E; }
            tr.problemSummary.effortTRIVIAL td.level { color: #50A40E; }
            tr.problemSummary.effortCOMPLEX td.level { color: #0065AC; }
            tr.problemSummary.effortREDESIGN td.level { color: #C67D00; }
            tr.problemSummary.effortARCHITECTURAL td.level { color: #C42F0E; }
            tr.problemSummary.effortUNKNOWN td.level { color: #C42F0E; }
            */
        </style>

        <#assign problemsBySeverity = getProblemSummaries(event, reportModel.projectModel, reportModel.includeTags, reportModel.excludeTags)>

        <script src="resources/js/jquery-3.3.1.min.js"></script>
        <script type="text/javascript" src="data/issue_summaries.js"></script>
        <script>
            // Set applicationId here so that it's available to all scripts
            <#if reportModel.projectModel??>
                let appId = ${reportModel.projectModel.getElement().id()?c};
            <#else>
                let appId = "allIssues";
            </#if>

            <#assign categories = getIssueCategories(event)>
            let categories = {
                <#list categories?keys as key>
                    "${key}": "${categories[key]}",
                </#list>
            };

            let problemSummaryNumbers = {
                <#list problemsBySeverity?keys as severity>
                    "${categories[severity]}": {
                        <#list problemsBySeverity[severity] as problemSummary>
                        "${problemSummary.id}": {
                            "numberFound": ${problemSummary.numberFound},
                            "storyPoints": ${problemSummary.effortPerIncident * problemSummary.numberFound}
                        },
                        </#list>
                    },
                </#list>
            }

            // Load scripts sequentially
            var dataScript = typeof appId !== "undefined" ? "data/sources_and_targets-" + appId + ".js" : "data/sources_and_targets-allIssues.js";
            let filteringScript = "resources/js/windup-issues-filtering.js";
            var scriptURLs = [dataScript, filteringScript];
            function loadScript(index) {
                if (index >= scriptURLs.length) {
                    return false;
                }

                var el = document.createElement('script');
                el.onload = () => loadScript(index + 1);
                el.src = scriptURLs[index];
                document.head.appendChild(el);
            }

            loadScript(0);
        </script>
    </head>
    <body role="document" class="migration-issues">
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
                        <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="${reportModel.description}"></i>
                        <#if !(reportModel.projectModel??) && reportModel.isExportAllIssuesCSV>
                        <span style="float: right;">
                            <a class="csvReport" style="font-size: 14pt;" align="right" href="../AllIssues.csv">(Download All Issues CSV)</a>
                        </span>
                        </#if>
                        </div>
                        <#if reportModel.projectModel??>
                            <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                        </#if>
                    </h1>
                </div>
            </div>

            <#assign sourcesAndTargets = getSourcesAndTargets(problemsBySeverity)>
            <#if sourcesAndTargets.getTargetTechs()?has_content || sourcesAndTargets.getSourceTechs()?has_content>
            <div class="container-fluid">
                <div class="row toolbar-pf">
                    <div class="col-sm-3">
                        <form class="toolbar-pf-actions" id="filter-form">
                            <div class="form-group toolbar-pf-filter" id="filter-div" style="padding-left: 0;">
                                <label class="sr-only filter-by" for="filter">Sources</label>
                                <div class="input-group">
                                    <#if sourcesAndTargets.getSourceTechs()?has_content>
                                    <div class="input-group-btn">
                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                                aria-haspopup="true"
                                                aria-expanded="false"><span class="filter-by">Sources</span> <span class="caret"></span></button>
                                        <ul class="dropdown-menu" id="dropdown-sources">
                                            <#list sourcesAndTargets.getSourceTechs() as st>
                                                <li><a href="#">${st}</a></li>
                                            </#list>
                                        </ul>
                                    </div>
                                    </#if>
                                    <#if sourcesAndTargets.getTargetTechs()?has_content>
                                    <div class="input-group-btn">
                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                                aria-haspopup="true"
                                                aria-expanded="false"><span class="filter-by">Targets</span> <span class="caret"></span></button>
                                        <ul class="dropdown-menu" id="dropdown-targets">
                                            <#list sourcesAndTargets.getTargetTechs() as tt>
                                                <li><a href="#">${tt}</a></li>
                                            </#list>
                                        </ul>
                                    </div>
                                    </#if>
                                    <div class="input-group-btn" id="filter-type">
                                        <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown"
                                                aria-haspopup="true"
                                                aria-expanded="false"><span class="filter-type" id="op-button">Matches any filter (OR)</span> <span class="caret"></span></button>
                                        <ul class="dropdown-menu">
                                            <li><a id="op-and" href="#">Matches all filters (AND)</a></li>
                                            <li><a id="op-or" href="#">Matches any filter (OR)</a></li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </form>
                    </div><!-- /col -->
                </div><!-- /row -->
                <div id="selected-filters">
                    <#if sourcesAndTargets.getSourceTechs()?has_content>
                        <div class="selected-tech">
                            Selected sources:
                            <div class="selected-item" id="selected-sources"></div>
                        </div>
                    </#if>
                    <#if sourcesAndTargets.getTargetTechs()?has_content>
                        <div class="selected-tech">
                            Selected targets:
                            <div class="selected-item" id="selected-targets"></div>
                        </div>
                    </#if>
                    <div class="inline-drop" id="clear"><a href="#">Clear all filters</a></div>
                </div>
            </div>
            </#if>

            <div class="row">
                <div class="container-fluid theme-showcase" role="main">
                    <!-- HEAD -->
                    <#if !problemsBySeverity?has_content>
                        <div>
                            No issues were found by the existing rules. If you would like to add custom rules,
                            see the <a href="https://access.redhat.com/documentation/en-us/red_hat_application_migration_toolkit/4.2/html/rules_development_guide/">
                            Rule Development Guide</a>.
                        </div>
                    </#if>
                    <#list problemsBySeverity?keys as severity>
                        <table id="table-${categories[severity]}" class="table table-bordered table-condensed tablesorter migration-issues-table">
                            <thead>
                                <tr class="tablesorter-ignoreRow" style="background: #337ab7;color: #FFFFFF; font-size: 14pt;">
                                    <td style="border: 0px; padding: 10px 15px"><b>${severity}</b></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                </tr>
                                <tr style="background: rgb(212, 230, 233);">
                                    <th class="sortable">Issue by Category</th>
                                    <th class="sortable-right text-right">Incidents Found</th>
                                    <th class="sortable-right text-right">Story Points per Incident</th>
                                    <th>Level of Effort</th>
                                    <th class="sortable-right text-right">Total Story Points</th>
                                </tr>
                            </thead>
                            <tfoot>
                                <tr class="tablesorter-ignoreRow" style="background: rgb(212, 230, 233);">
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px" class="text-right">${getIncidentsFound(problemsBySeverity[severity])}</td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px"></td>
                                    <td style="border: 0px" class="text-right">${getTotalPoints(problemsBySeverity[severity])}</td>
                                </tr>
                            </tfoot>
                            <tbody>
                                  <#list problemsBySeverity[severity] as problemSummary>
                                      <@migrationIssuesRenderer problemSummary />
                                  </#list>
                            </tbody>
                        </table>
                    </#list>
                    <!-- 4c47fc7cb... Several style fixes to make the reports more consistent -->
                </div>
            </div>
            <#include "include/timestamp.ftl">
        </div>


        <script src="resources/js/jquery-ui.min.js"></script>
        <script src="resources/js/bootstrap.min.js"></script>
        <script src="resources/js/jquery.tablesorter.min.js"></script>
        <script src="resources/js/jquery.tablesorter.widgets.min.js"></script>
        <script src="resources/libraries/handlebars/handlebars.4.0.5.min.js"></script>

        <!#-- We are using short variable names because they repeat a lot in the generated HTML
              and using short reduces the file sizes significantly.

              {l} - label
              {oc} - occurences
              {h} - href
              {t} - link title

              The actual data are generated lower into MIGRATION_ISSUES_DETAILS[] .
        -->
        <#noparse>
        <script id="detail-row-template" type="text/x-handlebars-template">
            {{#each problemSummaries}}
                {{#each files}}
                    <tr class="fileSummary tablesorter-childRow fileSummary_id_{{../problemSummaryID}}">
                        <td>
                            <div class="indent">
                                {{{l}}}
                            </div>
                        </td>
                        <td class="text-right">
                            {{oc}}
                        </td>

                        {{#if @first}}
                            <td colspan="3" rowspan="{{../files.length}}">
                                <div class="panel panel-default hint-detail-panel">
                                    <div class="panel-heading">
                                        <h4 class="panel-title pull-left">Issue Detail: {{../issueName}}</h4>
                                        {{#if ../ruleID}}
                                            <div class="pull-right">
                                                <a class="sh_url" title="{{../ruleID}}" href="windup_ruleproviders.html#{{../ruleID}}">Show Rule</a>
                                            </div>
                                        {{/if}}
                                        <div class="clearfix"></div>
                                    </div>
                                    <div class="panel-body">
                                        {{{../description}}}

                                    {{#if ../resourceLinks}}
                                        <div class="panel-body">
                                            <ul>
                                                {{#each ../resourceLinks}}
                                                    <li><a href="{{h}}" target="_blank">{{t}}</a></li>
                                                {{/each}}
                                            </ul>
                                        </div>
                                    {{/if}}

                                    {{#if ../sts}}
                                    Sources:</br>
                                    <div class="tech-list">
                                        {{#each ../sts}}
                                        <span class="technology source">{{this}}</span>
                                        {{/each}}
                                    </div>
                                    {{/if}}

                                    {{#if ../tts}}
                                    Targets:</br>
                                    <div class="tech-list">
                                        {{#each ../tts}}
                                        <span class="technology target">{{this}}</span>
                                        {{/each}}
                                    </div>
                                    {{/if}}
                                    </div>

                                    </br>
                                </div>
                            </td>
                        {{/if}}
                    </tr>
                {{/each}}
            {{/each}}
        </script>
        </#noparse>

        <script src="resources/js/windup-migration-issues.js"></script>
        <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>

        <#include "include/problem_summary.ftl">
        <#include "include/sources_targets.ftl">

    </body>
</html>
