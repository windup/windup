<!DOCTYPE html>
<html lang="en">

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<#macro mdbRenderer mdb>
    <tr>
        <td><@render_link model=mdb.ejbDeploymentDescriptor project=reportModel.projectModel text=mdb.beanName/></td>
        <td>
            <@render_link model=mdb.ejbClass project=reportModel.projectModel/>
        </td>
        <td>${(mdb.destination.jndiLocation)!}</td>
    </tr>
</#macro>


<#macro ejbRenderer ejb>
    <tr>
        <td>
            <@render_link model=ejb.ejbDeploymentDescriptor project=reportModel.projectModel text=ejb.beanName/>
        </td>
        <td style="text-align:center">
            <#if ejb.ejbHome??>
                <@render_link model=ejb.ejbHome project=reportModel.projectModel text="Home" class="btn btn-xxs btn-success"/><#t>
            <#else>
                <a style="visibility:hidden" class="btn btn-xxs btn-default disabled">Local</a><#t>
            </#if>
            <#if ejb.ejbLocal??>
                <@render_link model=ejb.ejbLocal project=reportModel.projectModel text="Local" class="btn btn-xxs btn-success"/><#t>
            <#else>
                <a style="visibility:hidden" class="btn btn-xxs btn-default disabled">Local</a><#t>
            </#if>
            <#if ejb.ejbRemote??>
                <@render_link model=ejb.ejbRemote project=reportModel.projectModel text="Remote" class="btn btn-xxs btn-danger"/><#t>
            <#else>
                <a style="visibility:hidden" class="btn btn-xxs btn-default disabled">Remote</a><#t>
            </#if>
        </td>
        <td>
            <@render_link model=ejb.ejbClass project=reportModel.projectModel/>
        </td>
        <td>
        	${(ejb.jndiReference.jndiLocation)!}
        </td>
    </tr>
</#macro>

<#macro entityRenderer ejb>
    <tr>
        <td>
            <@render_link model=ejb.ejbDeploymentDescriptor project=reportModel.projectModel text=ejb.beanName />
        </td>
        <td>
            <@render_link model=ejb.ejbClass project=reportModel.projectModel/>
        </td>
        <td>${ejb.tableName!""}</td>
        <td>${ejb.persistenceType!""}</td>
    </tr>
</#macro>


<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - EJB Report</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet">
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen">
    <link href="resources/img/tackle-icon.png" rel="shortcut icon" type="image/x-icon"/>

    <script src="resources/js/jquery-3.3.1.min.js"></script>
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
                    <div class="main">EJB Report
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists the Java EE EJB beans with their JNDI address - stateless and statefull beans, message driven beans, and entity beans."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

            <#if !reportModel.relatedResources.mdb?has_content && !reportModel.relatedResources.stateless?has_content && !reportModel.relatedResources.stateful?has_content && !reportModel.relatedResources.entity?has_content>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">EJB Report</h3>
                    </div>
                    <div class="panel-body">
                        <h3 class="panel-title">No EJBs found to report</h3>
                    </div>
                </div>
            </#if>

            <#list reportModel.relatedResources.mdb>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Message Driven Beans</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="mdbTable">
                        <tr>
                            <th class="col-md-2">MDB Name</th><th>Class</th><th class="col-md-3">JMS Destination</th>
                        </tr>
                        <#items as mdb>
                            <@mdbRenderer mdb />
                        </#items>
                    </table>
                </div>
            </#list>


            <#list reportModel.relatedResources.stateless>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Stateless Session Beans</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="statelessTable">
                        <tr>
                            <th class="col-md-2">Bean Name</th><th style="width:130px">Interface</th><th>Implementation</th><th class="col-md-3">JNDI Location</th>
                        </tr>
                        <#items as statelessBean>
                            <@ejbRenderer statelessBean/>
                        </#items>
                    </table>
                </div>
            </#list>

            <#list reportModel.relatedResources.stateful>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Stateful Session Beans</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="statefulTable">
                        <tr>
                          <th class="col-md-2">Bean Name</th><th style="width:130px">Interface</th><th>Implementation</th><th class="col-md-3">JNDI Location</th>
                        </tr>
                        <#items as statefulBean>
                            <@ejbRenderer statefulBean/>
                        </#items>
                    </table>
                </div>
            </#list>

            <#list reportModel.relatedResources.entity>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Entity Beans</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="entityTable">
                        <tr>
                        <th class="col-md-2">Bean Name</th><th>Class</th><th class="col-md-2">Table</th><th class="col-md-1">Persistence Type</th>
                        </tr>
                        <#items as entityBean>
                            <@entityRenderer entityBean/>
                        </#items>
                    </table>
                </div>
            </#list>
        </div> <!-- /container -->
    </div> <!-- /row-->

    <#include "include/timestamp.ftl">

    </div><!-- /container main-->

    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
