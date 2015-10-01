<ul class="nav navbar-nav">
<#if applicationReportIndexModel ??>
    <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as reportModel>
        <#if reportModel.reportIconClass?has_content>
            <li><a href="${reportModel.reportFilename}"><i class="${reportModel.reportIconClass}"></i> ${reportModel.reportName}</a></li>
        <#else>
            <li><a href="${reportModel.reportFilename}">${reportModel.reportName}</a></li>
        </#if>
    </#list>
</#if>
</ul>
<ul class="nav navbar-nav navbar-right">
    <#include "userfeedback.ftl">
</ul>
