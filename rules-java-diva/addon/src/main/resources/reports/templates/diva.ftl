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
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists the Java EE EJB beans with their JNDI address - stateless and statefull beans, message driven beans, and entity beans."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <#list reportModel.relatedResources.contexts as context>

        <div class="row panel">

            <div class="panel-heading panel-collapsed clickable">
            <span class="pull-left"><i class="glyphicon glyphicon-expand arrowIcon"></i></span>
            <table class="table">
            <#list context.constraints as constraint>
            <#if constraint.methodName??>
            <tr><td>entry class</td><td>${constraint.javaClass.qualifiedName}</td></tr>
            <tr><td>entry method</td><td>${constraint.methodName}</td></tr>
            </#if>
            </#list>
            <#list context.constraints as constraint>
            <#if constraint.paramName??>
            <tr><td>${constraint.paramName}</td><td>${constraint.paramValue}</td></tr>
            </#if>
            </#list>
            </table>
            </div>

            <div class="panel-body" style="display:none">
            <#list context.transactions as tx>
            <#list tx.ops as op>

            <#list stackTraceToList(op.stackTrace)>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">${op.sql}</h3>
                    </div>
                    <table class="table table-striped table-bordered">
                        <#items as location>
                        <tr>
                           <td>
                               <@render_link model=location project=reportModel.projectModel/>
                           </td>
                        </tr>
                        </#items>
                    </table>
                </div>
            </#list>
            </#list>
            </#list>
            </div> <!-- panel-body -->

        </div> <!-- row panel -->

        </#list>

    <#include "include/timestamp.ftl">

    </div><!-- /container main-->

    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
