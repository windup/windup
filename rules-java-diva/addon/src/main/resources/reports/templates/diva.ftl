<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - EJB Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/img/mta-icon.png" rel="shortcut icon" type="image/x-icon"/>

    <script src="resources/js/jquery-3.3.1.min.js"></script>

    <script>
    function togglePanelSlide(event) {
        var $panelHeading = $(this);
        setPanelSlide($panelHeading, $panelHeading.hasClass("panel-collapsed"));
    }

    function setPanelSlide($panelHeading, expand) {
        var projectGuid = $panelHeading.parent().data("windup-projectguid");
        // $.sessionStorage.set(projectGuid, expand ? "true" : "false");
        $panelHeading.parents(".panel").find(".panel-body")["slide" + (expand ? "Down" : "Up")]();
        $panelHeading.parents(".panel").toggleClass("panel-boarding", expand);
        $panelHeading.toggleClass("panel-collapsed", !expand);
        $panelHeading.find("i").toggleClass("glyphicon-expand", !expand).toggleClass("glyphicon-collapse-up", expand);
    }
    $(document).on("click", ".panel-heading", function(event) {
       togglePanelSlide.call(this, event);
    });
    </script>
</head>
<body role="document">

  <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
    <div class="wu-navbar-header navbar-header">
      <#include "include/navheader.ftl">
    </div>
    <div class="navbar-collapse collapse navbar-responsive-collapse">
      <#include "include/navbar.ftl">
    </div><!-- /.nav-collapse -->
  </div>

  <div class="container-fluid" role="main">
    <div class="row">
      <div class="page-header page-header-no-border">
        <h1>
          <div class="main">Transactions Report
            <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists the code analysis results for JDBC or JPA transactions."></i></div>
          <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
        </h1>
      </div>
    </div>

    <#assign keys = [] />
    <#list reportModel.relatedResources.contexts as context >
      <#list context.constraints as constraint >
        <#if constraint.paramName?? && !keys?seq_contains(constraint.paramName)>
          <#assign keys += [ constraint.paramName ] />
        </#if>
      </#list>
    </#list>
    <#assign width = keys?size + 2/>

      <div class="row">
          <div class="container-fluid">
              <div class="panel panel-primary">
                  <div class="panel-body">
                      <table class="table">
                          <thead>
                          <tr style="display:table-row" >
                              <th>entry class</th>
                              <th>entry method</th>
                              <#list keys as key>
                                  <th>${key}</th>
                              </#list>
                          </tr>
                          </thead>
                          <tbody>

                          <#list reportModel.relatedResources.contexts as context>
                              <#assign cs = {} />
                              <#list context.constraints as constraint>
                                  <#if constraint.methodName?? >
                                      <#assign cs += {
                                      "k0": constraint.javaClass.qualifiedName,
                                      "k1": constraint.methodName } />
                                  </#if>
                                  <#if constraint.paramName?? && keys?seq_contains(constraint.paramName) >
                                      <#assign cs += {
                                      "k" + (keys?seq_index_of(constraint.paramName) + 2): constraint.paramValue } />
                                  </#if>
                              </#list>

                              <tr style="display:table-row" >
                                  <#list 0..(width - 1) as i >
                                      <td>
                                          <#if i == 0>
                                              <a data-toggle="collapse" href="#entry_${context?index}" aria-expanded="false" aria-controls="entry_${context?index}"> &gt; </a>
                                          </#if>
                                          ${ cs["k" + i]! }
                                      </td>
                                  </#list>
                              </tr>
                              <tr class="collapse" id="entry_${context?index}">
                                  <td colspan="${width + 2}" style="padding-left: 20px;">
                                      <#list context.transactions as tx>
                                          <#list tx.ops as op>
                                              <div>
                                                  <table class="table table-striped">
                                                      <thead>
                                                      <tr>
                                                          <th style="font-weight: normal;">
                                                              <a data-toggle="collapse" href="#op_${context?index}_${tx?index}_${op?index}" aria-expanded="false" aria-controls="op_${context?index}_${tx?index}_${op?index}"> &gt; </a>
                                                              ${op.sql}
                                                          </th>
                                                      </tr>
                                                      </thead>
                                                      <tbody class="collapse" id="op_${context?index}_${tx?index}_${op?index}">
                                                      <#list stackTraceToList(op.stackTrace) as trace>
                                                          <tr>
                                                              <td style="padding-left: 20px;">
                                                                  <@render_link model=trace.location project=reportModel.projectModel/>
                                                              </td>
                                                          </tr>
                                                      </#list>
                                                      </tbody>
                                                  </table>
                                              </div>
                                          </#list>
                                      </#list>
                                  </td>
                              </tr>
                          </#list>

                          </tbody>
                      </table>
                  </div>
              </div>
          </div>
      </div>

    <#include "include/timestamp.ftl" />

  </div><!-- /container main-->

  <script src="resources/js/bootstrap.min.js"></script>
  <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
