<ul class="nav navbar-nav">
<#if applicationReportIndexModel ??>
    <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as navReportModel>
        <#assign liClass = "">
        <#assign reportUrl = navReportModel.reportFilename>
        <#if navUrlPrefix??>
            <#assign reportUrl = "${navUrlPrefix}${reportUrl}">
        </#if>

        <#if reportModel?? && reportModel.equals(navReportModel) >
            <#assign liClass = "active">
            <#assign reportUrl = "#">
        </#if>

        <#if navReportModel.reportIconClass?has_content>
            <li class="${liClass}">
                <a href="${reportUrl}">
                    <i class="${navReportModel.reportIconClass}"></i> ${navReportModel.reportName}
                </a>
            </li>
        <#else>
            <li class="${liClass}">
                <a href="${reportUrl}">${navReportModel.reportName}</a>
            </li>
        </#if>
    </#list>
</#if>
</ul>
<ul class="nav navbar-nav navbar-right">
    <#include "userfeedback.ftl">
</ul>
