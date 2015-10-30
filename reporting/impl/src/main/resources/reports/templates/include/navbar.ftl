<ul class="nav navbar-nav">
<#if applicationReportIndexModel ??>
    <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as navbarReportModel>
        <#assign liClass = "">

        <#if reportModel?? && reportModel.equals(navbarReportModel)>
            <#assign liClass = "active">
        </#if>

        <#if navbarReportModel.reportIconClass?has_content>
            <li class="${liClass}"><a href="${navbarReportModel.reportFilename}"><i class="${navbarReportModel.reportIconClass}"></i> ${navbarReportModel.reportName}</a></li>
        <#else>
            <li class="${liClass}"><a href="${navbarReportModel.reportFilename}">${navbarReportModel.reportName}</a></li>
        </#if>
    </#list>
</#if>
</ul>
<ul class="nav navbar-nav navbar-right">
    <#include "userfeedback.ftl">
</ul>
