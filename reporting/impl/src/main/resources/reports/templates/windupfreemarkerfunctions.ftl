<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>Migration Toolkit for Applications by Red Hat FreeMarker Functions and Directives</title>
    <link href="resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="resources/css/font-awesome.min.css" rel="stylesheet" />
    <link href="resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link href="resources/css/windup.java.css" rel="stylesheet" media="screen"/>
    <link href="resources/img/mta-icon-128.png" rel="shortcut icon" type="image/x-icon"/>
</head>
<body role="document">

    <div id="main-navbar" class="navbar navbar-inverse navbar-fixed-top">
        <div class="wu-navbar-header navbar-header">
            <#include "include/navheader.ftl">
        </div>
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <ul class="nav navbar-nav">
                <li><a href="../index.html"><i class="glyphicon glyphicon-arrow-left"></i> All Applications</a></li>
            </ul>
        </div><!-- /.nav-collapse -->
        <div class="navbar-collapse collapse navbar-responsive-collapse">
            <ul class="nav navbar-nav"></ul>
        </div><!-- /.nav-collapse -->
    </div>

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Used FreeMarker Functions and Directives
                    <i class="glyphicon glyphicon-info-sign" data-toggle="tooltip" data-placement=right title="This report shows the custom Freemarker extensions created for and used by Migration Toolkit for Applications by Red Hat ."></i></div>
                </h1>
            </div>
        </div>

        <div class="row container-fluid">
            <div class="panel panel-primary">
                <div class="panel-heading">
                    Functions
                </div>
                <table class="table table-striped table-bordered">
                    <tr>
                        <th>Function Name</th>
                        <th>Description</th>
                        <th>Implemented by</th>
                    </tr>
                    <#list getAllFreeMarkerMethods() as freeMarkerMethod>
                        <tr>
                            <td>${freeMarkerMethod.name}</td>
                            <td>${freeMarkerMethod.description}</td>
                            <td>${freeMarkerMethod.class}</td>
                        </tr>
                    </#list>
                </table>
            </div>

            <div class="panel panel-primary">
                <div class="panel-heading">
                    Directives
                </div>
                <table class="table table-striped table-bordered">
                    <tr>
                        <th>Directive Name</th>
                        <th>Directive Class</th>
                        <th>Description</th>
                    </tr>
                    <#list getAllFreeMarkerDirectives() as freeMarkerDirective>
                        <tr>
                            <td>${freeMarkerDirective.name}</td>
                            <td>${freeMarkerDirective.class}</td>
                            <td>${freeMarkerDirective.description} (Implemented by: ${freeMarkerDirective.class})</td>
                        </tr>
                    </#list>
                </table>
            </div>

            <div>
                FreeMarker version used: ${.version}
            </div>
        </div> <!-- /row -->

    </div> <!-- /container main -->

    <script src="resources/js/jquery-3.3.1.min.js"></script>

    <script src="resources/libraries/flot/jquery.flot.min.js"></script>
    <script src="resources/libraries/flot/jquery.flot.pie.min.js"></script>

    <script src="resources/js/bootstrap.min.js"></script>
    <script type="text/javascript" src="resources/js/navbar.js"></script>
    <script>$(document).ready(function(){$('[data-toggle="tooltip"]').tooltip();});</script>
</body>
</html>
