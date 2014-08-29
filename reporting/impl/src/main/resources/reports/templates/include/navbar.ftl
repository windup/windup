<li><a href="index.html">All Applications</a></li>
<#list applicationReportIndexModel.applicationReportModelsSortedByPriority as reportModel>
  <li><a href="${reportModel.reportFilename}">${reportModel.reportName}</a></li>
</#list>