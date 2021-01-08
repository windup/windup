<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Spring Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/img/mta-icon.png" rel="shortcut icon" type="image/x-icon"/>

    <script src="resources/js/jquery-3.3.1.min.js"></script>
</head>
<body role="document">

    <!-- Navbar -->
    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <#include "include/navbar.ftl">
        </div>
    </div>
    <!-- / Navbar -->

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Spring Bean Report
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="The Spring bean report lists the SpringBeans found in the application - their name and the implementing class."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Spring Beans</h3>
                    </div>

                    <#if !iterableHasContent(reportModel.relatedResources.springBeans)>
                    <div class="panel-body">
                        No Spring Beans Found!
                    </div>
                    </#if>
                    <#if iterableHasContent(reportModel.relatedResources.springBeans)>
                        <table class="table table-striped table-bordered" id="springBeansTable">
                            <tr>
                                <th>Bean Name</th><th>Java Class</th>
                            </tr>
                            <#list reportModel.relatedResources.springBeans as springBean>
                                <tr>
                                   <td><@render_link model=springBean.springConfiguration project=reportModel.projectModel text=springBean.springBeanName/></td>
                                   <td><@render_link model=springBean.javaClass project=reportModel.projectModel/></td>
                                </tr>
                            </#list>
                        </table>
                    </#if>
                </div><!--end of panel-->
            </div> <!-- /container -->
        </div><!-- /row -->

    <#include "include/timestamp.ftl">
    </div><!-- /container main -->

    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
