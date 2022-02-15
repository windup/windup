<!DOCTYPE html>

<#assign applicationReportIndexModel = reportModel.applicationReportIndexModel>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>${reportModel.projectModel.name} - Remote Services</title>
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
        </div><!-- /.nav-collapse -->
    </div>
    <!-- / Navbar -->

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Remote Services
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report lists Java EE EJB services - their interfaces and implementations."></i></div>
                    <div class="path">${reportModel.projectModel.rootFileModel.applicationName}</div>
                </h1>
            </div>
        </div>

        <div class="row">
            <div class="container-fluid theme-showcase" role="main">

            <#list reportModel.relatedResources.jaxRsServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">REST Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Service Path</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>${service.path}</td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>

            <#list reportModel.relatedResources.jaxWsServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">JAX-WS Services (SOAP)</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>

            <#list reportModel.relatedResources.ejbRemoteServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Remote EJB Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>

            <#list reportModel.relatedResources.rmiServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">RMI Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>
           <#list reportModel.relatedResources.amqpServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">AMQP Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>
            <#list reportModel.relatedResources.jmsServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">JMS Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>
            <#list reportModel.relatedResources.hessianServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Hessian Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>
            <#list reportModel.relatedResources.httpinvokerServices>
                <div class="panel panel-primary">
                    <div class="panel-heading">
                        <h3 class="panel-title">Http Invoker Services</h3>
                    </div>
                    <table class="table table-striped table-bordered" id="jpaEntityTable">
                        <tr>
                            <th class="col-md-6">Interface</th><th class="col-md-6">Implementation</th>
                        </tr>
                        <#items as service>
                            <tr>
                                <td>
                                    <@render_link model=service.interface project=reportModel.projectModel/>
                                </td>
                                <td>
                                    <@render_link model=service.implementationClass project=reportModel.projectModel/>
                                </td>
                            </tr>
                        </#items>
                    </table>
                </div>
            </#list>

        </div> <!-- /container -->
    </div><!--/row-->
    <#include "include/timestamp.ftl">
    </div><!-- /container main-->


    <script src="resources/js/bootstrap.min.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
  </body>
</html>
