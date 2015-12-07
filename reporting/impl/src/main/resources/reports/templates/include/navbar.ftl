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

        
            <li class="${liClass}">
                <a href="${reportUrl}">
                  <#if navReportModel.reportIconClass?has_content>
                    <i class="${navReportModel.reportIconClass}"></i>
                  </#if>
                  ${navReportModel.reportName}
                </a>
            </li>
        
    </#list>
</#if>
</ul>
<ul class="nav navbar-nav navbar-right">
    <#include "userfeedback.ftl">
</ul>
