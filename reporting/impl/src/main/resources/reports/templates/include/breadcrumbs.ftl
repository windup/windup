<#list reportModel.allParentsInReversedOrder as report>
<#if report.reportName == "Overview" && report.projectModel??>
  <li><a href="${report.reportFilename}">${report.projectModel.name}</a></li>
<#else>
  <li><a href="${report.reportFilename}">${report.reportName}</a></li>
</#if>  
  
</#list>
