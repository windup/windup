<ul class="nav navbar-nav">
<#if applicationReportIndexModel ??>
    <#-- This is for adding Application List link aka index.html-->
    <#if !index_page!false>
        <li class="">
            <a href="../index.html"><i class="glyphicon glyphicon-home"></i> All Applications</a>
        </li>
    <#else>
        <li class="active">
            <a href="#"><i class="glyphicon glyphicon-home"></i> All Applications</a>
        </li>
    </#if>

    <#list applicationReportIndexModel.applicationReportModelsSortedByPriority as navReportModel>
        <#if navReportModel.displayInApplicationReportIndex>
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
        </#if>
    </#list>
</#if>
</ul>
<ul class="nav navbar-nav navbar-right">
    <#include "userfeedback.ftl">
</ul>

<script type="text/javascript">
    var script   = document.createElement("script");
    script.type  = "text/javascript";
    script.src   = "resources/js/navbar.js";
    document.body.appendChild(script);
</script>