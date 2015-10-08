<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <title>${application.applicationName} - Server Resource Report</title>
    <link href="../../resources/css/bootstrap.min.css" rel="stylesheet"/>
    <link href="../../resources/css/windup.css" rel="stylesheet" media="screen"/>
  </head>
  <body role="document">

    <div class="container-fluid" role="main">
        <div class="row">
            <div class="page-header page-header-no-border">
                <h1>
                    <div class="main">Server Resource Report</div>
                    <div class="path">${application.applicationName?html}</div>
                </h1>
                <div class="navbar navbar-default">
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
	<#if server.databases?has_content>
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
				<#list server.databases as databaseResource>
                	<tr>
                		<td>${databaseResource.databaseType}</td>
                		<td>${databaseResource.jndiName}</td>
                	</tr>
                </#list>
              </table>
            </div>
          </div>
        </div>
      </div>
	</#if>

	<#if server.queues?has_content>
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
				<#list server.queues as jmsResource>
                	<tr>
                		<td>${jmsResource.jmsType}</td>
                		<td>${jmsResource.jndiName}</td>
                	</tr>
                </#list>
              </table>
            </div>
          </div>
        </div>
    </div>
    </#if>

    <#if server.jmxBeans?has_content>
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
				<#list server.jmxBeans as jmxResource>
                	<tr>
                		<td>${jmxResource.jmxObjectName}</td>
                		<td>${jmxResource.qualifiedName}</td>
                	</tr>
                </#list>
              </table>
            </div>
          </div>
        </div>
      </div>
    </#if>


    </div> <!-- /container -->


    <script src="https://code.jquery.com/jquery.js"></script>
    <script src="../../resources/js/bootstrap.min.js"></script>
  </body>
</html>