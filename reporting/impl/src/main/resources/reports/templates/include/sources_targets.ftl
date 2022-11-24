<#if reportModel.projectModel??>
    <#assign reportId = reportModel.projectModel.getElement().id()?c/>
<#else>
    <#assign reportId = "allIssues"/>
</#if>

<#assign filename = "sources_and_targets-${reportId}.js"/>
<@write_to_disk filename="${filename}">
WINDUP_TECHNOLOGIES["${reportId}"] = {
    <#assign sourceTechs = sourcesAndTargets.getSourceTechs()/>
    <#if sourceTechs?has_content>
        sourceTechs: [
        <#list sourceTechs as tech>
            "${tech}",
        </#list>
        ],
    </#if>

    <#assign targetTechs = sourcesAndTargets.getTargetTechs()/>
    <#if targetTechs?has_content>
        targetTechs: [
        <#list targetTechs as tech>
            "${tech}",
        </#list>
        ],
    </#if>

    <#assign issuesBySourceTech = sourcesAndTargets.getIssuesBySourceTech()/>
        issuesBySource: {
        <#if issuesBySourceTech?has_content>
            <#list sourcesAndTargets.getIssuesBySourceTech() as tech, issues>
                "${tech}": [
                <#list issues as issue>
                    "${issue}",
                </#list>
                ],
            </#list>
        </#if>
        },

        <#assign issuesByTargetTech = sourcesAndTargets.getIssuesByTargetTech()/>
        issuesByTarget: {
        <#if issuesByTargetTech?has_content>
            <#list sourcesAndTargets.getIssuesByTargetTech() as tech, issues>
                "${tech}": [
                <#list issues as issue>
                    "${issue}",
                </#list>
                ],
            </#list>
        </#if>
    }
}
</@write_to_disk>