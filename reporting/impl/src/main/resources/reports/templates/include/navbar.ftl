<#if applicationReportIndexModel ??>
<#list applicationReportIndexModel.applicationReportModelsSortedByPriority as reportModel>
  <li><a href="${reportModel.reportFilename}">${reportModel.reportName}</a></li>
</#list>
</#if>
