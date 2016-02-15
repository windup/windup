<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>${application.applicationName} - Server Resource Report</title>
    <link href="../../resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../../resources/css/windup.css" rel="stylesheet" media="screen"/>
    <link href="../../resources/img/favicon.png" rel="shortcut icon" type="image/x-icon"/>
</head>
<body role="document">

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Server Resource Report</div>
                    <div class="path">${application.applicationName?html}</div>
                </h1>
                <div class="desc">
                    This reports lists the resources of the application that are supposed to be used by the server,
                    such as datasources, JMS destinations, JMS connection factories, JMS connection factory list, and thread pools.
                </div>

                <div id="main-navbar" class="navbar navbar-default">
                    <div class="navbar-header">
                        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                            <span class="icon-bar"></span>
                        </button>
                    </div>
                    <div class="navbar-collapse collapse navbar-responsive-collapse">
                        <ul class="nav navbar-nav">
                            <li><a href="index.html">Application</a></li>
                            <li><a href="ejbs.html">EJBs</a></li>
                            <li><a href="hibernate.html">Hibernate</a></li>
                            <li><a href="spring.html">Spring</a></li>
                            <li><a href="server-resources.html">Server Resources</a></li>
                            <li><a href="classloader-blacklists.html">Blacklists</a></li>
                            <li><a href="classloader-duplicate.html">Duplicates</a></li>
                            <li><a href="classloader-notfound.html">Not Found</a></li>
                        </ul>
                    </div><!-- /.nav-collapse -->
                </div>
            </div>
        </div>

    </div>



    <div class="container-fluid theme-showcase" role="main">
    <#list server.databases>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">Database Resource</h3>
            </div>
            <div class="panel-body">
                <div class="row">
                    <div class="col-md-6">
                      <div class="well well-sm" style="height: 300px;">Graph of MDB to Queue</div>
                    </div>
                    <div class="col-md-6">
                        <table class="table table-striped table-bordered">
                            <tr>
                                <th>Database Type</th><th>JNDI Name</th>
                            </tr>
                            <#items as databaseResource>
                                <tr>
                                    <td>${databaseResource.databaseType}</td>
                                    <td>${databaseResource.jndiName}</td>
                                </tr>
                            </#items>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </#list>

    <#list server.queues>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">JMS Resources</h3>
            </div>
            <div class="panel-body">
              <div class="row">
                <div class="col-md-6">
                  <div class="well well-sm" style="height: 300px;">Graph of MDB to Queue</div>
                </div>
                <div class="col-md-6">
                  <table class="table table-striped table-bordered">
                    <tr>
                      <th>JMS Type</th><th>JNDI Name</th>
                    </tr>
                    <#items as jmsResource>
                        <tr>
                            <td>${jmsResource.jmsType}</td>
                            <td>${jmsResource.jndiName}</td>
                        </tr>
                    </#items>
                  </table>
                </div>
              </div>
            </div>
        </div>
    </#list>

    <#list server.jmxBeans>
        <div class="panel panel-primary">
            <div class="panel-heading">
                <h3 class="panel-title">JMX Resources</h3>
            </div>
            <div class="panel-body">
                <div class="row">
                    <div class="col-md-6">
                        <div class="well well-sm" style="height: 300px;">Graph of MDB to Queue</div>
                    </div>
                    <div class="col-md-6">
                        <table class="table table-striped table-bordered">
                            <tr>
                                <th>Object Name</th><th>Java Class</th>
                            </tr>
                            <#items as jmxResource>
                                <tr>
                                    <td>${jmxResource.jmxObjectName}</td>
                                    <td>${jmxResource.qualifiedName}</td>
                                </tr>
                            </#items>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </#list>


    </div> <!-- /container -->


    <script src="https://code.jquery.com/jquery.js"></script>
    <script src="../../resources/js/bootstrap.min.js"></script>
</body>
</html>